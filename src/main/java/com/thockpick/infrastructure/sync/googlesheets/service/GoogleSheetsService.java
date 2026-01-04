package com.thockpick.infrastructure.sync.googlesheets.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.services.CommonGoogleClientRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.thockpick.infrastructure.sync.googlesheets.dto.SwitchSheetRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Google Sheets API를 사용하여 스프레드시트 데이터를 읽어오는 서비스
 */
@Slf4j
@Service
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "Thock Pick";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${GOOGLE_SHEETS_SPREADSHEET_ID}")
    private String spreadsheetId;

    @Value("${GOOGLE_SHEETS_RANGE}")
    private String range;

    @Value("${GOOGLE_SHEETS_API_KEY}")
    private String apiKey;

    /**
     * Google Sheets에서 스위치 데이터 읽어오기
     * 
     * @return 스위치 데이터 리스트
     */
    public List<SwitchSheetRow> readSwitchData() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
                    .setApplicationName(APPLICATION_NAME)
                    .setGoogleClientRequestInitializer(new CommonGoogleClientRequestInitializer(apiKey))
                    .build();

            // 1. 전체 시트(탭) 목록 가져오기
            Spreadsheet spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
            List<Sheet> sheets = spreadsheet.getSheets();

            List<SwitchSheetRow> allRows = new ArrayList<>();

            log.info("총 {}개의 탭을 발견했습니다. 데이터 수집을 시작합니다.", sheets.size());

            // 2. 각 탭을 순회하며 데이터 읽기
            for (Sheet sheet : sheets) {
                String sheetTitle = sheet.getProperties().getTitle();

                // 탭 이름으로 범위를 만듭니다 (예: 'Leobog(7)'!A:Z)
                // 탭 이름에 공백이나 특수문자가 있을 수 있어 작은따옴표('')로 감쌉니다.
                String fullRange = String.format("'%s'!%s", sheetTitle, range);

                try {
                    ValueRange response = service.spreadsheets().values()
                            .get(spreadsheetId, fullRange)
                            .execute();

                    List<List<Object>> values = response.getValues();

                    // 해당 탭의 데이터를 파싱해서 결과 리스트에 추가
                    List<SwitchSheetRow> parsedRows = parseSheetValues(values, sheetTitle);
                    if (!parsedRows.isEmpty()) {
                        log.info("탭 '{}' 에서 {}개의 스위치 데이터를 읽었습니다.", sheetTitle, parsedRows.size());
                        allRows.addAll(parsedRows);
                    } else {
                        log.debug("탭 '{}' 에는 유효한 스위치 데이터가 없습니다.", sheetTitle);
                    }

                } catch (Exception e) {
                    log.warn("탭 '{}' 읽기 실패 (무시하고 계속 진행): {}", sheetTitle, e.getMessage());
                }
            }

            return allRows;

        } catch (GeneralSecurityException | IOException e) {
            log.error("Google Sheets 데이터 읽기 실패", e);
            throw new RuntimeException("Google Sheets 데이터 읽기 실패", e);
        }
    }

    /**
     * 하나의 시트 데이터(Values)를 파싱하여 객체 리스트로 변환
     */
    private List<SwitchSheetRow> parseSheetValues(List<List<Object>> values, String sheetTitle) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 헤더 행 찾기
        int headerRowIndex = -1;
        Map<String, Integer> headerMap = new HashMap<>();

        for (int i = 0; i < values.size(); i++) {
            List<Object> row = values.get(i);
            boolean isHeader = row.stream()
                    .map(Object::toString)
                    .anyMatch(cell -> cell.replaceAll("\\s+", "").contains("스위치이름"));

            if (isHeader) {
                headerRowIndex = i;
                for (int j = 0; j < row.size(); j++) {
                    String headerName = row.get(j).toString().replaceAll("\\s+", "");
                    headerMap.put(headerName, j);
                }
                break;
            }
        }

        // 헤더("스위치이름")가 없는 탭(예: "시트 읽는 법")은 조용히 빈 리스트 반환
        if (headerRowIndex == -1) {
            return Collections.emptyList();
        }

        // 2. 데이터 파싱
        return IntStream.range(headerRowIndex + 1, values.size())
                // sheetTitle 파라미터 추가 전달
                .mapToObj(i -> SwitchSheetRow.from(i + 1, values.get(i), headerMap, sheetTitle))
                .filter(row -> row.getName() != null && !row.getName().isEmpty())
                .collect(Collectors.toList());
    }



}
