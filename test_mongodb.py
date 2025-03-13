#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from pymongo import MongoClient
from datetime import datetime

def test_mongodb_connection():
    """MongoDB 연결 테스트"""
    try:
        # MongoDB 연결
        client = MongoClient('mongodb://localhost:27017/')
        print("MongoDB 연결 성공")
        
        # 데이터베이스 목록 확인
        db_names = client.list_database_names()
        print(f"데이터베이스 목록: {db_names}")
        
        # turtledream 데이터베이스 선택
        db = client['turtledream']
        print(f"turtledream 데이터베이스 선택 성공")
        
        # 컬렉션 목록 확인
        collections = db.list_collection_names()
        print(f"컬렉션 목록: {collections}")
        
        # posture_data 컬렉션 확인
        if 'posture_data' in collections:
            # 데이터 조회
            posture_data = list(db.posture_data.find())
            print(f"posture_data 컬렉션에 {len(posture_data)}개의 문서가 있습니다.")
            
            # 최근 5개 문서 출력
            for doc in posture_data[-5:]:
                print(f"문서: {doc}")
        else:
            print("posture_data 컬렉션이 없습니다.")
            
            # 테스트 데이터 삽입
            print("테스트 데이터 삽입 시도...")
            result = db.posture_data.insert_one({
                'userId': 'test_user',
                'isGoodPosture': True,
                'postureStatus': 'GOOD',
                'feedback': '테스트 피드백',
                'recordedAt': datetime.now(),
                'badPostureDuration': 0,
                'totalSessionDuration': 0,
                'createdAt': datetime.now()
            })
            print(f"테스트 데이터 삽입 성공: {result.inserted_id}")
        
        return True
    except Exception as e:
        print(f"MongoDB 연결 테스트 중 오류 발생: {str(e)}")
        return False

if __name__ == "__main__":
    print("MongoDB 연결 테스트 시작")
    result = test_mongodb_connection()
    print(f"MongoDB 연결 테스트 결과: {'성공' if result else '실패'}") 