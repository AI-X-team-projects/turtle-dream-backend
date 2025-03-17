# Turtle Dream Backend

## OAuth2 로그인 구현 가이드 (업데이트)

### 백엔드 설정

1. 구글 개발자 콘솔에서 OAuth2 클라이언트 ID와 시크릿을 발급받습니다.
2. 리다이렉트 URI를 `http://localhost:8080/login/oauth2/code/google`로 설정합니다.
3. 환경 변수 또는 application.yml 파일에 다음 설정을 추가합니다:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: [구글 클라이언트 ID]
            client-secret: [구글 클라이언트 시크릿]
            redirect-uri: "{baseUrl}/login/oauth2/code/google"
            scope: email, profile

app:
  frontend:
    url: http://localhost:3000 # 프론트엔드 URL
```

### 프론트엔드 구현

1. 구글 로그인 버튼 클릭 시 다음 URL로 리다이렉트합니다:

```javascript
// 리다이렉트 URI를 지정하지 않는 경우 (권장)
window.location.href = "http://localhost:8080/oauth2/authorize/google";

// 리다이렉트 URI를 지정하는 경우
const redirectUri = encodeURIComponent("http://localhost:3000/oauth2/redirect");
window.location.href = `http://localhost:8080/oauth2/authorize/google?redirect_uri=${redirectUri}`;
```

2. 로그인 성공 시 프론트엔드의 `/oauth2/redirect` 페이지로 리다이렉트됩니다. URL에는 다음 파라미터가 포함됩니다:

   - `login=success`: 로그인 성공 여부
   - `userId=[사용자 ID]`: 로그인한 사용자의 ID

3. OAuth2RedirectHandler 컴포넌트 구현 예시:

```jsx
// OAuth2RedirectHandler.jsx
import React, { useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";

const OAuth2RedirectHandler = () => {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // URL에서 파라미터 추출
    const params = new URLSearchParams(location.search);
    const login = params.get("login");
    const userId = params.get("userId");

    if (login === "success" && userId) {
      // 사용자 정보 API 호출
      fetch("http://localhost:8080/api/oauth2/user", {
        method: "GET",
        credentials: "include", // 쿠키 포함
      })
        .then((response) => response.json())
        .then((data) => {
          if (data.authenticated) {
            // 로그인 성공 처리
            localStorage.setItem("user", JSON.stringify(data));

            // URL에서 파라미터 제거하고 메인 페이지로 이동
            navigate("/main", { replace: true });
          } else {
            // 로그인 실패 처리
            console.error("로그인 실패:", data.message);
            navigate("/login");
          }
        })
        .catch((error) => {
          console.error("API 호출 오류:", error);
          // API 호출 실패 시에도 URL 파라미터를 활용하여 로그인 처리
          localStorage.setItem("userId", userId);
          navigate("/main", { replace: true });
        });
    } else {
      // 로그인 실패 처리
      navigate("/login");
    }
  }, [navigate, location]);

  return (
    <div className="oauth2-redirect">
      <p>로그인 처리 중입니다...</p>
    </div>
  );
};

export default OAuth2RedirectHandler;
```

4. 라우터 설정:

```jsx
// App.jsx
import { BrowserRouter, Routes, Route } from "react-router-dom";
import OAuth2RedirectHandler from "./components/OAuth2RedirectHandler";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
        <Route path="/main" element={<MainPage />} />
        {/* 다른 라우트 */}
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

### 주의사항

1. CORS 설정: 프론트엔드와 백엔드의 도메인이 다른 경우 CORS 설정이 필요합니다.
2. 쿠키 설정: 세션 쿠키를 사용하므로 `credentials: 'include'` 옵션을 설정해야 합니다.
3. 리다이렉트 URI: 구글 개발자 콘솔에 등록된 리다이렉트 URI와 백엔드 설정의 리다이렉트 URI가 일치해야 합니다.

### 문제 해결

1. `/oauth2/authorize/google` 엔드포인트에서 오류가 발생하는 경우:

   - 구글 OAuth2 클라이언트 ID와 시크릿이 올바르게 설정되어 있는지 확인합니다.
   - 구글 개발자 콘솔에 등록된 리다이렉트 URI가 백엔드 설정과 일치하는지 확인합니다.

2. 로그인 성공 후 리다이렉트되지 않는 경우:

   - 백엔드의 OAuth2AuthenticationSuccessHandler 클래스에서 리다이렉트 URL이 올바르게 설정되어 있는지 확인합니다.
   - 프론트엔드 URL이 application.yml 파일에 올바르게 설정되어 있는지 확인합니다.

3. 한글 이름 인코딩 문제가 발생하는 경우:

   - 리다이렉트 URL에 한글 이름이 포함되지 않도록 설정합니다.
   - URL 파라미터에 한글이 포함된 경우 `URLEncoder.encode()` 메서드를 사용하여 인코딩합니다.

4. 로그인 후 사용자 정보를 가져올 수 없는 경우:
   - 세션이 올바르게 유지되고 있는지 확인합니다.
   - `/api/oauth2/user` API가 올바르게 구현되어 있는지 확인합니다.
   - 네트워크 탭에서 API 호출 결과를 확인합니다.
   - API 호출 실패 시에도 URL 파라미터를 활용하여 기본적인 로그인 처리를 할 수 있습니다.
