CREATE TABLE chatbot_response (
                                  keyword VARCHAR2(100) PRIMARY KEY,
                                  response CLOB
);
INSERT INTO chatbot_response (keyword, response) VALUES ('대여', '대여 가능한 물품은 자전거, 텐트, 캠핑의자입니다.');
INSERT INTO chatbot_response (keyword, response) VALUES ('날씨', '날씨 정보는 현재 준비 중입니다.');
INSERT INTO chatbot_response (keyword, response) VALUES ('반납', '반납은 대여 종료일 전에 가능합니다.');
COMMIT;
