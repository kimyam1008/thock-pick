/**
 * 스위치 목록 관리 모듈
 * 전역 변수 오염을 방지하기 위해 객체 리터럴 패턴 사용
 */
const SwitchListApp = {
    // 상태 관리
    state: {
        currentPage: 0,
        pageSize: 12,
        filters: {
            type: '',
            keyword: ''
        },
        apiUrl: ''
    },

    // DOM 요소 캐싱 (init에서 초기화)
    elements: {},

    // 초기화 함수
    init: function() {
        this.cacheElements();
        this.bindEvents();
        this.readUrlParams();

        // HTML에서 API URL 가져오기
        const container = document.getElementById('switchListContainer');
        if(container) {
            this.state.apiUrl = container.dataset.apiUrl;
        }

        this.loadSwitches(0);
    },

    // DOM 요소 찾기 및 저장
    cacheElements: function() {
        this.elements = {
            typeFilter: document.getElementById('typeFilter'),
            keywordFilter: document.getElementById('keywordFilter'),
            loading: document.getElementById('loading'),
            grid: document.getElementById('switchGrid'),
            pagination: document.getElementById('pagination'),
            totalCount: document.getElementById('totalCount'),
            noResults: document.getElementById('noResults')
        };
    },

    // 이벤트 리스너 등록
    bindEvents: function() {
        // 검색 버튼 (HTML onclick 대신 여기서 바인딩 권장)
        const searchBtn = document.querySelector('button[onclick="searchSwitches()"]');
        if(searchBtn) {
            searchBtn.onclick = () => this.searchSwitches();
            searchBtn.removeAttribute('onclick'); // HTML의 인라인 이벤트 제거
        }

        const resetBtn = document.querySelector('button[onclick="resetFilters()"]');
        if(resetBtn) {
            resetBtn.onclick = () => this.resetFilters();
            resetBtn.removeAttribute('onclick');
        }

        // 엔터키 입력
        this.elements.keywordFilter.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.searchSwitches();
        });
    },

    // URL 파라미터 읽기
    readUrlParams: function() {
        const urlParams = new URLSearchParams(window.location.search);
        const typeParam = urlParams.get('type');
        const keywordParam = urlParams.get('keyword');

        if (typeParam) {
            this.state.filters.type = typeParam;
            this.elements.typeFilter.value = typeParam;
        }
        if (keywordParam) {
            this.state.filters.keyword = keywordParam;
            this.elements.keywordFilter.value = keywordParam;
        }
    },

    // 검색 실행
    searchSwitches: function() {
        this.state.filters.type = this.elements.typeFilter.value;
        this.state.filters.keyword = this.elements.keywordFilter.value;
        this.loadSwitches(0);
    },

    // 필터 초기화
    resetFilters: function() {
        this.elements.typeFilter.value = '';
        this.elements.keywordFilter.value = '';
        this.state.filters = { type: '', keyword: '' };
        this.loadSwitches(0);
    },

    // 스위치 목록 로드 (async/await 사용으로 가독성 향상)
    loadSwitches: async function(page) {
        this.state.currentPage = page;
        this.toggleLoading(true);
        this.elements.grid.innerHTML = '';

        const params = new URLSearchParams({
            page: page,
            size: this.state.pageSize
        });

        if (this.state.filters.type) params.append('type', this.state.filters.type);
        if (this.state.filters.keyword) params.append('keyword', this.state.filters.keyword);

        try {
            // URL이 없으면 기본값 사용 (fallback)
            const targetUrl = this.state.apiUrl || '/switches/api';
            const response = await fetch(`${targetUrl}?${params.toString()}`);

            if (!response.ok) throw new Error('Network response was not ok');

            const data = await response.json();

            this.toggleLoading(false);
            this.renderSwitches(data.switches);
            this.renderPagination(data);
            this.elements.totalCount.textContent = data.totalElements;

            this.elements.noResults.style.display = data.switches.length === 0 ? 'block' : 'none';

        } catch (error) {
            console.error('Error loading switches:', error);
            this.toggleLoading(false);
            alert('데이터를 불러오는 중 오류가 발생했습니다.');
        }
    },

    // 로딩 상태 토글
    toggleLoading: function(show) {
        if(this.elements.loading) {
            this.elements.loading.style.display = show ? 'block' : 'none';
        }
    },

    // 스위치 목록 렌더링
    renderSwitches: function(switches) {
        switches.forEach(sw => {
            const card = document.createElement('div');
            card.className = 'switch-card';

            // 헬퍼 함수로 중복 제거
            const createInfoRow = (label, value, suffix = '') => {
                if (!value) return '';
                return `<div class="switch-info-row">
                    <span class="switch-info-label">${label}:</span>
                    <span>${value}${suffix}</span>
                </div>`;
            };

            const infoHtml = [
                createInfoRow('제조사', sw.manufacturer),
                createInfoRow('카테고리', sw.category),
                createInfoRow('작동압', sw.actuationForce, 'g'),
                createInfoRow('바닥압', sw.bottomOutForce, 'g'),
                createInfoRow('가격', sw.price, '원')
            ].join('');

            card.innerHTML = `
                <span class="switch-type ${sw.type}">${sw.type}</span>
                <div class="switch-name">${sw.name}</div>
                <div class="switch-info">${infoHtml}</div>
                <a class="detail-link" href="/switches/${sw.id}">상세보기 →</a>
            `;
            this.elements.grid.appendChild(card);
        });
    },

    // 페이징 렌더링
    renderPagination: function(data) {
        const pagination = this.elements.pagination;
        pagination.innerHTML = '';

        if (data.totalPages <= 1) return;

        const createPageItem = (text, page, isActive = false, isDisabled = false) => {
            const li = document.createElement('li');
            li.className = `page-item ${isActive ? 'active' : ''} ${isDisabled ? 'disabled' : ''}`;

            const a = document.createElement('a');
            a.className = 'page-link';
            a.href = '#';
            a.innerHTML = text;

            if (!isDisabled) {
                a.onclick = (e) => {
                    e.preventDefault();
                    this.loadSwitches(page);
                };
            }
            li.appendChild(a);
            return li;
        };

        // 이전 버튼
        pagination.appendChild(createPageItem('이전', data.currentPage - 1, false, !data.hasPrevious));

        // 페이지 번호
        const startPage = Math.max(0, data.currentPage - 4);
        const endPage = Math.min(data.totalPages, startPage + 10);

        for (let i = startPage; i < endPage; i++) {
            pagination.appendChild(createPageItem(i + 1, i, i === data.currentPage));
        }

        // 다음 버튼
        pagination.appendChild(createPageItem('다음', data.currentPage + 1, false, !data.hasNext));
    }
};

// 앱 초기화
document.addEventListener('DOMContentLoaded', () => {
    SwitchListApp.init();
});