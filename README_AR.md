# تطبيق تحويل الرسائل البنكية (Android)

## أسرع طريقة لاستخراج ملف APK جاهز للتثبيت (بدون Android Studio)
1. أنشئ مستودعاً جديداً (New Repository) على موقع [GitHub](https://github.com).
2. فك الضغط عن الملف وارفع محتويات مجلد `BankSmsForwarder` إلى المستودع (بما فيها مجلد `.github`).
3. افتح تبويب **Actions** في المستودع، وانتظر دقيقتين حتى تنتهي عملية البناء `Build Android APK` بالعلامة الخضراء.
4. اضغط على العملية المكتملة ثم نزّل ملف **`BankSmsForwarder-APK`** من قسم Artifacts، وفك الضغط عنه لتحصل على ملف `app-debug.apk` الجاهز للتثبيت مباشرة على جوالك.

## طريقة البناء عبر الكمبيوتر (Android Studio)
1. افتح مجلد `BankSmsForwarder` في Android Studio.
2. اختر `Build` ← `Build Bundle(s) / APK(s)` ← `Build APK(s)`.
3. انقل `app-debug.apk` إلى هاتفك وثبّته.
