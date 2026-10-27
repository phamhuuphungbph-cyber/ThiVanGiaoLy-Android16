# Thi Văn Giáo Lý PGHH — Android 16

Dự án xây dựng lại ứng dụng Android (Java), đọc ngoại tuyến 215 bài viết/10 nhóm trích từ dữ liệu SQLite của APK do người dùng cung cấp. Nội dung được giữ nguyên trong `app/src/main/assets/thivan.sqlite`.

## Cách tạo APK qua GitHub

1. Tạo repository mới trên GitHub, tải toàn bộ **nội dung thư mục này** lên nhánh `main` (bao gồm cả thư mục `.github/workflows`).
2. Vào **Actions** → **Build Android 16 APK** → **Run workflow** (hoặc push mã nguồn vào `main`).
3. Chọn lần chạy thành công → **Artifacts** → tải `ThiVanGiaoLy-Android16-APK`.
4. Giải nén, chép `app-debug.apk` lên điện thoại và cài đặt.

**Lưu ý:** APK phiên bản mới có package `vn.pghh.thivangiaoly` và chữ ký debug khác APK cũ; có thể cài song song. Nếu không cài được hãy gỡ bản ứng dụng có trùng package trước khi cài. Không cung cấp APK này khi chưa kiểm thử trên máy Android 16.

Thông số: `compileSdk 36`, `targetSdk 35`, `minSdk 23`, Java 17, Android Gradle Plugin 8.9.2.

Tính năng: toàn bộ dữ liệu offline; tìm kiếm tiêu đề/nội dung; lọc theo 10 nhóm; lưu yêu thích; căn giữa nội dung; tăng giảm cỡ chữ; không yêu cầu quyền Internet.
