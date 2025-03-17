# OAuth2 로그인 구현 가이드 (프론트엔드)

## 1. 백엔드 OAuth2 엔드포인트 정보

프론트엔드에서 OAuth2 로그인을 구현하기 위해 필요한 백엔드 엔드포인트 정보입니다.

```javascript
const AUTH_ENDPOINTS = {
  // Google 로그인 시작 URL
  GOOGLE_AUTH_URL: "/oauth2/authorize/google",

  // 로그인 성공 후 리다이렉트 URL
  // 성공 시 /main?login=success&name={인코딩된_이름} 으로 리다이렉트됩니다.

  // 사용자 정보 조회 URL
  USER_INFO_URL: "/api/oauth2/user",

  // 세션 정보 조회 URL
  SESSION_INFO_URL: "/api/oauth2/session",

  // 현재 로그인한 사용자 정보 조회 URL (기존 로그인 API와 동일한 형식)
  CURRENT_USER_URL: "/api/oauth2/current-user",

  // 로그아웃 URL
  LOGOUT_URL: "/api/user/logout",
};
```

## 2. 구현 시 고려사항

1. **URL 인코딩**: 로그인 성공 후 리다이렉트 URL에 포함된 `name` 파라미터는 URL 인코딩되어 있습니다. 이를 디코딩하여 사용해야 합니다.

   ```javascript
   // URL에서 name 파라미터 추출 및 디코딩
   const urlParams = new URLSearchParams(window.location.search);
   const encodedName = urlParams.get("name");
   const name = encodedName ? decodeURIComponent(encodedName) : "";
   ```

2. **쿠키 기반 인증**: 백엔드는 세션 기반 인증을 사용합니다. API 요청 시 쿠키를 포함하도록 설정해야 합니다.

   ```javascript
   // API 요청 시 쿠키 포함 설정
   fetch("/api/oauth2/user", {
     method: "GET",
     credentials: "include", // 중요: 쿠키를 포함하기 위한 설정
     headers: {
       "Content-Type": "application/json",
     },
   })
     .then((response) => response.json())
     .then((data) => console.log(data));
   ```

3. **CORS 설정**: 백엔드는 `http://localhost:3000`에서의 요청을 허용하도록 설정되어 있습니다. 프론트엔드가 다른 URL에서 실행되는 경우 백엔드의 CORS 설정을 변경해야 합니다.

## 3. 구현 예시

### 3.1 Google 로그인 버튼 컴포넌트

```jsx
import React from "react";

const GoogleLoginButton = () => {
  const handleGoogleLogin = () => {
    window.location.href = "/oauth2/authorize/google";
  };

  return (
    <button onClick={handleGoogleLogin} className="google-login-btn">
      Google로 로그인
    </button>
  );
};

export default GoogleLoginButton;
```

### 3.2 OAuth2 리다이렉트 처리 컴포넌트

```jsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const OAuth2RedirectHandler = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const loginStatus = urlParams.get("login");
    const encodedName = urlParams.get("name");
    const name = encodedName ? decodeURIComponent(encodedName) : "";

    if (loginStatus === "success") {
      // 로그인 성공 시 사용자 정보 조회
      fetch("/api/oauth2/user", {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("사용자 정보를 가져오는데 실패했습니다.");
          }
          return response.json();
        })
        .then((data) => {
          // 사용자 정보 저장 (Redux, Context API 등 사용)
          localStorage.setItem("user", JSON.stringify(data));

          // 대시보드 페이지로 이동
          navigate("/dashboard");
        })
        .catch((error) => {
          setError(error.message);
          setLoading(false);
        });
    } else {
      setError("로그인에 실패했습니다.");
      setLoading(false);
    }
  }, [navigate]);

  if (loading) {
    return <div>로그인 처리 중...</div>;
  }

  if (error) {
    return <div>오류 발생: {error}</div>;
  }

  return null;
};

export default OAuth2RedirectHandler;
```

### 3.3 사용자 인증 상태 관리 (Context API 사용)

```jsx
import React, { createContext, useState, useEffect, useContext } from "react";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // 세션 확인
  const checkSession = async () => {
    try {
      const response = await fetch("/api/oauth2/session", {
        method: "GET",
        credentials: "include",
      });

      if (response.ok) {
        const data = await response.json();
        if (data.authenticated) {
          setUser(data);
        } else {
          setUser(null);
        }
      } else {
        setUser(null);
      }
    } catch (error) {
      console.error("세션 확인 중 오류 발생:", error);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  // 로그아웃
  const logout = async () => {
    try {
      await fetch("/api/user/logout", {
        method: "POST",
        credentials: "include",
      });
      setUser(null);
      window.location.href = "/login";
    } catch (error) {
      console.error("로그아웃 중 오류 발생:", error);
    }
  };

  useEffect(() => {
    checkSession();
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, checkSession, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
```

### 3.4 보호된 라우트 컴포넌트

```jsx
import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "./AuthContext";

const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return <div>로딩 중...</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default ProtectedRoute;
```

### 3.5 라우터 설정

```jsx
import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import OAuth2RedirectHandler from "./components/OAuth2RedirectHandler";

const App = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/main" element={<OAuth2RedirectHandler />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
```

### 3.6 로그인 페이지

```jsx
import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import GoogleLoginButton from "../components/GoogleLoginButton";

const Login = () => {
  const { user, loading } = useAuth();

  if (loading) {
    return <div>로딩 중...</div>;
  }

  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <div className="login-container">
      <h1>로그인</h1>
      <GoogleLoginButton />
    </div>
  );
};

export default Login;
```

## 4. 백엔드 CORS 설정

백엔드의 CORS 설정은 다음과 같이 되어 있습니다. 프론트엔드가 `http://localhost:3000` 이외의 URL에서 실행되는 경우 이 설정을 변경해야 합니다.

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // 프론트엔드 URL
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true); // 쿠키 및 인증 정보 허용

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```
