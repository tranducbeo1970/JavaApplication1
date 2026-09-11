# Chay X400PositiveDrMsSender

Tat ca cau hinh nam trong cac hang so o dau X400PositiveDrMsSender.java.
Chay main class DataSet.X400PositiveDrMsSender trong NetBeans, de trong Program Arguments.
Khong can bien moi truong cho password, bind DN hay dia chi.
Moi lan Run se thu ket noi P7 va submit REPORT; khong con nhanh chi in Usage.

- REPORT_RECIPIENT: VVTSAMHS, nhan DR.
- BIND_OR_ADDRESS va DELIVERED_RECIPIENT: VVTSOPTA.
- PASSWORD, BIND_DN, PRESENTATION_ADDRESS: cau hinh ket noi theo class IPN.
- ORIGINAL_MESSAGE_ID: hien la ID MAU test-original-message. Thay bang message identifier
  P1/envelope cua dien goc de dau thu ghep DR voi dien do. Khong dung IPM ID.
- ORIGINAL_RECIPIENT_NUMBER = 1, ORIGINAL_CONTENT_ID = ContentId, OEIT = ia5-text:
  du lieu thu; thay theo dien goc neu can bao cao cho dien thuc.
- ARRIVAL_TIME va DELIVERY_TIME: mac dinh lay luc chay cho ban thu nghiem.
- NEW_REPORT_ID: tu sinh moi moi lan chay.

Libraries: C:\Program Files\Isode\bin\java\classes\isode-x400.jar
VM Options: -Djava.library.path="C:\Program Files\Isode\bin"
Native DLL va Java phai cung kien truc 32/64 bit; Isode bin can nam trong PATH.

Log OK cho biet tung thao tac API thanh cong. STATUS=ERROR in ten thao tac,
ma loi va mo ta. STATUS=SUBMITTED chi co nghia API tra thanh cong, chua xac nhan
report den dau thu. Neu SDK/P7 tu choi report, class khong chuyen sang MT hoac IPN.

Day van la ban thu nghiem X400ms/P7. Isode tai lieu hoa viec tu tao DR o Gateway API:
https://www.isode.com/edi-systems/
Source UA tham khao chi cho thay gui message/probe/IPN va doc DR.
Code UA va X400IpnSender goc khong bi sua.