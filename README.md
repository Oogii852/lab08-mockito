# Лаборатори №8: Mock объектууд — Mockito

**Оюутан:** Б. Отгонсүх  
**Код:** B242270021  
**Хичээл:** F.CSA313

## Орчин

```
$ java -version
openjdk version "25.0.4.1" 2026-08-18
OpenJDK Runtime Environment (build 25.0.4.1+1-1-26.04.4-Ubuntu)
OpenJDK 64-Bit Server VM (build 25.0.4.1+1-1-26.04.4-Ubuntu, mixed mode, sharing)

$ mvn -version
Apache Maven 3.9.12
Maven home: /usr/share/maven
Java version: 25.0.4.1, vendor: Ubuntu, runtime: /usr/lib/jvm/java-25-openjdk-amd64
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "7.0.0-38-generic", arch: "amd64", family: "unix"
```

## Тестийн тоо

`results/mvn-test.txt`-ийн сүүлийн мөр: **Tests run: 10, Failures: 0, Errors: 0, Skipped: 0**, `BUILD SUCCESS`.
(7 энгийн тест + A8-ийн 3 мөр.)

## A8 дахь lenient() тайлбар

A8 параметржүүлсэн тестийн мөр бүр өөр өөр stub-ийн хэсгийг ашигладаг. Жишээ нь "оюутан байхгүй" мөрөнд `courseExists`, `prerequisites` stub хэзээ ч дуудагдахгүй. MockitoExtension-ий strict stubs горим ашиглагдаагүй stub-ийг `UnnecessaryStubbingException`-ээр унагаадаг тул `lenient().when(...)` хэрэглэсэн.

## Хэсэг B: мини мутаци

| Мутант | Эвдрэл | Унасан тестүүд | Үр дүн |
| --- | --- | --- | --- |
| 1 | `if (!student.active())` шалгалтыг комментлосон | `register_inactiveStudent_returnsInactiveAndDoesNotTouchEmail` (A3), `register_errorScenarios_returnExpectedErrorCode[2]` (A8, `true,false,false`) | Failures: 2, BUILD FAILURE |
| 2 | `repository.saveRegistration(...)` дуудалтыг комментлосон | `register_activeStudent_returnsOkSavesAndSendsEmail` (A1), `register_emailSenderThrows_stillReturnsOkAndSaves` (A5) | Failures: 2, BUILD FAILURE |
| 3 | `\|\| courseId == null`-ийг устгасан | `register_nullArgument_throwsIllegalArgumentAndDoesNotTouchDependencies` (A6) | Failures: 1, BUILD FAILURE |

Файлууд: `results/mvn-test-mutant-1.txt`, `results/mvn-test-mutant-2.txt`, `results/mvn-test-mutant-3.txt`.

**Дүгнэлт (мутаци):** Гурван мутантыг бүгдийг тест барьсан. A6 нь `studentId`, `courseId` хоёр аргументийн `null`-ийг тусад нь шалгадаг тул 3-р мутантыг шууд барьсан, тестийн цоорхой олдоогүй тул нэмж засах шаардлагагүй болсон. Мутаци бүрийн дараа `git checkout -- src/main`-аар кодыг сэргээж, эцэст нь `results/mvn-test.txt`-ийг дахин ногоон гаргасан.

## Дүгнэлт

Mock нь Mockito-гоор динамикаар үүсдэг, дуудалтыг бүртгэж авдаг объект бөгөөд stub болон verify-аар зан төлөвийг нь шалгадаг. Гараар бичсэн fake нь жинхэнэ хэрэгжүүлэлтийг хялбарчилсан хувилбар (жишээ нь Map-тай repository) бөгөөд төлөвөөр нь шалгадаг. Fake-ийг өөрөө бичиж, засварлах хэрэгтэй, харин mock-ийг тест бүрт хэдхэн мөрөөр тохируулна. Өөрийн бизнес логик, Student гэх мэт энгийн өгөгдлийн объект, List, String зэрэг стандарт төрлийг mock хийх ёсгүй, учир нь тэгвэл бодит логикийг биш өөрийн таамгийг шалгана. Verify-ийг хэтрүүлбэл тест хэрэгжүүлэлтийн дотоод дарааллаас хэт хамаарч, зөв рефакторинг хийхэд ч унадаг эмзэг тест болно. Иймээс үр дүнг assert-ээр, зөвхөн чухал харилцан үйлдлийг (хадгалсан эсэх, и-мэйл явсан эсэх) verify-аар шалгах нь зөв. Mock-оор тестлэх нь хурдан бөгөөд сан, SMTP сервер шаардахгүй, харин бодит интеграцийг шалгадаггүй тул тусдаа интеграцийн тест хэрэгтэй хэвээр.
