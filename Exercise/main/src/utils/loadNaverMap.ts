let loaded = false;
let loading = false;
let loadPromise: Promise<void> | null = null;

export function loadNaverMap(clientId: string, submodules = "geocoder"): Promise<void> {
  // 이미 로드되었고 naver.maps가 존재하면 즉시 반환
  if (loaded && typeof (window as any).naver !== 'undefined' && typeof (window as any).naver.maps !== 'undefined') {
    return Promise.resolve();
  }
  
  // 이미 로딩 중이면 같은 Promise 반환
  if (loading && loadPromise) {
    return loadPromise;
  }
  
  loading = true;
  
  loadPromise = new Promise((resolve, reject) => {
    // 이미 스크립트가 로드되어 있는지 확인
    const existingScript = document.querySelector(`script[src*="oapi.map.naver.com"]`);
    
    if (existingScript) {
      console.log('네이버 지도 스크립트가 이미 존재합니다. 초기화 대기 중...');
      
      // naver.maps가 로드될 때까지 기다림 (최대 5초)
      let attempts = 0;
      const maxAttempts = 50;
      
      const checkInterval = setInterval(() => {
        attempts++;
        
        if (typeof (window as any).naver !== 'undefined' && typeof (window as any).naver.maps !== 'undefined') {
          clearInterval(checkInterval);
          loaded = true;
          loading = false;
          console.log('네이버 지도 API 초기화 완료!');
          resolve();
        } else if (attempts >= maxAttempts) {
          clearInterval(checkInterval);
          loading = false;
          loaded = false;
          reject(new Error('네이버 지도 API 초기화 타임아웃. 페이지를 새로고침해주세요.'));
        }
      }, 100);
      
      return;
    }
    
    // 새 스크립트 생성
    const script = document.createElement("script");
    script.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpClientId=${clientId}&submodules=${submodules}`;
    script.async = true;
    
    console.log('네이버 지도 스크립트 로드 시작:', script.src);
    
    // 타임아웃 처리 (30초)
    const timeout = setTimeout(() => {
      loading = false;
      loaded = false;
      loadPromise = null;
      console.error('네이버 지도 API 로드 타임아웃');
      reject(new Error('네이버 지도 API 로드 타임아웃 (30초).\n\n인터넷 연결을 확인하고 페이지를 새로고침해주세요.'));
    }, 30000);
    
    script.onload = () => {
      console.log('네이버 지도 스크립트 로드됨. 초기화 대기 중...');
      clearTimeout(timeout);
      
      // naver.maps가 완전히 초기화될 때까지 기다림
      let attempts = 0;
      const maxAttempts = 50; // 최대 5초
      
      const checkInterval = setInterval(() => {
        attempts++;
        
        if (typeof (window as any).naver !== 'undefined' && typeof (window as any).naver.maps !== 'undefined') {
          clearInterval(checkInterval);
          loaded = true;
          loading = false;
          console.log('네이버 지도 API 초기화 완료!');
          resolve();
        } else if (attempts >= maxAttempts) {
          clearInterval(checkInterval);
          loading = false;
          loaded = false;
          loadPromise = null;
          console.error('네이버 지도 API 초기화 실패');
          reject(new Error('네이버 지도 API가 제대로 로드되지 않았습니다.\n\n다음을 확인해주세요:\n1. Exercise/main/.env 파일에 VITE_NAVER_CLIENT_ID 설정\n2. 네이버 클라우드 콘솔에서 서비스 URL 등록:\n   - http://localhost:5173\n   - http://localhost:5174\n3. 개발 서버 재시작 (npm run dev)\n4. 브라우저 캐시 삭제 후 새로고침'));
        }
      }, 100);
    };
    
    script.onerror = (error) => {
      clearTimeout(timeout);
      loading = false;
      loaded = false;
      loadPromise = null;
      console.error('네이버 지도 스크립트 로드 실패:', error);
      
      reject(new Error('네이버 지도 API를 불러오는데 실패했습니다.\n\n다음을 확인해주세요:\n1. Exercise/main/.env 파일에 다음 내용 추가:\n   VITE_NAVER_CLIENT_ID=발급받은_클라이언트ID\n\n2. 네이버 클라우드 플랫폼(https://console.naver.com/ncloud/application)에서:\n   - Maps > Application 선택\n   - 서비스 환경 > Web Dynamic Map\n   - 서비스 URL에 다음 추가:\n     http://localhost:5173/*\n     http://localhost:5174/*\n\n3. 개발 서버 재시작:\n   npm run dev\n\n4. 인터넷 연결 상태 확인'));
    };
    
    document.head.appendChild(script);
  });
  
  return loadPromise;
}


