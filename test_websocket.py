#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import websocket
import json
import base64
import time
import threading
from datetime import datetime

# 테스트 이미지 파일 경로
TEST_IMAGE_PATH = "test_image.jpg"

# 웹소켓 URL
WS_URL = "ws://localhost:8080/ws/posture"

def log(message):
    """로그 메시지 출력"""
    print(f"[{datetime.now().isoformat()}] {message}")

def on_message(ws, message):
    """메시지 수신 핸들러"""
    log(f"서버로부터 메시지 수신: {message}")
    try:
        data = json.loads(message)
        log(f"자세 상태: {'좋음' if data.get('isGoodPosture') else '나쁨'}")
        log(f"피드백: {data.get('feedback')}")
    except Exception as e:
        log(f"메시지 처리 중 오류: {str(e)}")

def on_error(ws, error):
    """오류 핸들러"""
    log(f"오류 발생: {str(error)}")

def on_close(ws, close_status_code, close_msg):
    """연결 종료 핸들러"""
    log(f"연결 종료: {close_status_code} - {close_msg}")

def on_open(ws):
    """연결 성공 핸들러"""
    log("웹소켓 연결 성공")
    
    def run():
        # 사용자 등록 메시지 전송
        register_message = json.dumps({
            "type": "REGISTER",
            "userId": "test_user"
        })
        log("사용자 등록 메시지 전송")
        ws.send(register_message)
        time.sleep(1)
        
        # 이미지 데이터 전송
        try:
            with open(TEST_IMAGE_PATH, "rb") as image_file:
                encoded_string = base64.b64encode(image_file.read()).decode('utf-8')
                
            image_message = json.dumps({
                "type": "IMAGE",
                "userId": "test_user",
                "imageData": f"data:image/jpeg;base64,{encoded_string}"
            })
            log("이미지 데이터 전송")
            ws.send(image_message)
            
            # 10초 대기 후 연결 종료
            time.sleep(10)
            ws.close()
        except Exception as e:
            log(f"이미지 전송 중 오류: {str(e)}")
            ws.close()
    
    threading.Thread(target=run).start()

def test_websocket():
    """웹소켓 연결 테스트"""
    log(f"웹소켓 연결 시도: {WS_URL}")
    
    # 웹소켓 연결
    ws = websocket.WebSocketApp(WS_URL,
                              on_open=on_open,
                              on_message=on_message,
                              on_error=on_error,
                              on_close=on_close)
    
    # 웹소켓 연결 실행
    ws.run_forever()

if __name__ == "__main__":
    # websocket 모듈 디버그 모드 활성화
    websocket.enableTrace(True)
    
    log("웹소켓 테스트 시작")
    test_websocket()
    log("웹소켓 테스트 종료") 