# 01. Java IO 스트림 기본 개념

## 🎯 학습 목표

- 스트림의 정의와 기본 개념 이해
- 바이트 스트림과 문자 스트림의 차이점 파악
- Java IO 스트림의 계층 구조 학습

## 📚 스트림이란?

### 정의

**스트림(Stream)**은 데이터를 순차적으로 읽거나 쓸 수 있는 데이터의 흐름을 의미합니다.

### 특징

- **단방향**: 입력 스트림은 읽기만, 출력 스트림은 쓰기만 가능
- **순차적**: 데이터를 순서대로 처리 (랜덤 액세스 불가)
- **연속성**: 데이터가 끊어지지 않고 연속적으로 흐름

### 비유

```
📁 파일 → 🚰 수도관 → 🥤 컵
(데이터)  (스트림)   (프로그램)
```

## 🔄 바이트 스트림 vs 문자 스트림

### 1. 바이트 스트림 (Byte Stream)

- **단위**: 8비트(1바이트) 단위로 데이터 처리
- **용도**: 이미지, 음악, 실행 파일 등 모든 종류의 파일
- **클래스**: `InputStream`, `OutputStream`을 상속받는 클래스들

```java
// 바이트 스트림 예시
FileInputStream fis = new FileInputStream("image.jpg");
int data = fis.read(); // 1바이트씩 읽기
```

### 2. 문자 스트림 (Character Stream)

- **단위**: 16비트(2바이트) 단위로 데이터 처리
- **용도**: 텍스트 파일, 로그 파일 등 문자 데이터
- **클래스**: `Reader`, `Writer`를 상속받는 클래스들

```java
// 문자 스트림 예시
FileReader reader = new FileReader("text.txt");
int data = reader.read(); // 1문자씩 읽기
```

## 🏗️ 스트림의 계층 구조

### InputStream 계층 구조

```
InputStream (추상 클래스)
├── FileInputStream (파일에서 읽기)
├── ByteArrayInputStream (바이트 배열에서 읽기)
├── BufferedInputStream (버퍼링된 읽기)
├── DataInputStream (기본 타입 데이터 읽기)
└── ObjectInputStream (객체 읽기)
```

### OutputStream 계층 구조

```
OutputStream (추상 클래스)
├── FileOutputStream (파일에 쓰기)
├── ByteArrayOutputStream (바이트 배열에 쓰기)
├── BufferedOutputStream (버퍼링된 쓰기)
├── DataOutputStream (기본 타입 데이터 쓰기)
└── ObjectOutputStream (객체 쓰기)
```

## 💡 핵심 개념 정리

### 1. 추상 클래스

- `InputStream`과 `OutputStream`은 추상 클래스
- 직접 인스턴스 생성 불가
- 구체적인 구현 클래스를 사용해야 함

```java
// ❌ 컴파일 에러
InputStream is = new InputStream(); // 추상 클래스는 인스턴스 생성 불가

// ✅ 정상 동작
InputStream is = new FileInputStream("file.txt");
```

### 2. 다형성 활용

- 상위 타입으로 선언하고 하위 타입으로 구현
- 코드의 유연성과 확장성 향상

```java
// 다양한 입력 소스에 대해 동일한 인터페이스 사용
InputStream input1 = new FileInputStream("file.txt");
InputStream input2 = new ByteArrayInputStream(bytes);
InputStream input3 = new BufferedInputStream(input1);

// 공통 메서드 사용
int data = input1.read();
int data2 = input2.read();
int data3 = input3.read();
```

### 3. 체이닝(Chaining)

- 여러 스트림을 연결하여 사용
- 각 스트림이 특정 기능을 담당

```java
// 파일 → 버퍼링 → 데이터 처리 순서로 연결
InputStream fileStream = new FileInputStream("data.dat");
InputStream bufferedStream = new BufferedInputStream(fileStream);
DataInputStream dataStream = new DataInputStream(bufferedStream);

// 데이터 읽기
int number = dataStream.readInt();
String text = dataStream.readUTF();
```

## 🧪 실습 예제

### 기본 스트림 사용법

```java
import java.io.*;

public class StreamBasicExample {
    public static void main(String[] args) {
        // 1. 파일에서 바이트 읽기
        try (FileInputStream fis = new FileInputStream("test.txt")) {
            int data;
            System.out.println("파일 내용 (바이트):");
            while ((data = fis.read()) != -1) {
                System.out.print((char) data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2. 바이트 배열에 데이터 쓰기
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            String text = "Hello, Stream!";
            baos.write(text.getBytes());

            byte[] result = baos.toByteArray();
            System.out.println("\n바이트 배열 크기: " + result.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

## ⚠️ 주의사항

### 1. 리소스 관리

- 스트림 사용 후 반드시 닫기
- `try-with-resources` 사용 권장

```java
// ❌ 잘못된 방법
FileInputStream fis = new FileInputStream("file.txt");
// ... 사용
fis.close(); // 예외 발생 시 close() 호출되지 않을 수 있음

// ✅ 권장 방법
try (FileInputStream fis = new FileInputStream("file.txt")) {
    // ... 사용
} // 자동으로 close() 호출
```

### 2. 예외 처리

- `IOException`은 체크 예외
- 반드시 try-catch 또는 throws 처리

```java
// ❌ 컴파일 에러
public void readFile() {
    FileInputStream fis = new FileInputStream("file.txt"); // IOException 처리 필요
}

// ✅ 정상 동작
public void readFile() throws IOException {
    FileInputStream fis = new FileInputStream("file.txt");
}

// 또는
public void readFile() {
    try (FileInputStream fis = new FileInputStream("file.txt")) {
        // ... 사용
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

## 🔗 다음 강의

**02. 입력 스트림** - `InputStream`과 관련 클래스들의 구체적인 사용법을 학습합니다.

- `FileInputStream`: 파일에서 데이터 읽기
- `BufferedInputStream`: 성능 최적화
- `ByteArrayInputStream`: 메모리 데이터 읽기

## 💡 정리

1. **스트림**은 데이터의 흐름을 의미하며, 순차적이고 단방향입니다.
2. **바이트 스트림**은 8비트 단위, **문자 스트림**은 16비트 단위로 처리합니다.
3. **계층 구조**를 이해하면 적절한 스트림을 선택할 수 있습니다.
4. **리소스 관리**와 **예외 처리**가 중요합니다.

이제 스트림의 기본 개념을 이해했으니, 다음 강의에서 구체적인 사용법을 학습해보세요! 🚀
