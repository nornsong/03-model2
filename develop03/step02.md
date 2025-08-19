# Step02: 이미지 썸네일 생성 시스템

## 🎯 목표

업로드된 이미지 파일에 대해 자동으로 썸네일을 생성하여 목록에서 빠른 미리보기를 제공하고, 이미지 로딩 성능을 향상시킵니다.

## ⚠️ 중요: 이미지 썸네일의 필요성

### 기존 시스템의 문제점

- **로딩 속도**: 원본 이미지 크기로 인한 느린 페이지 로딩
- **대역폭 낭비**: 목록에서 작은 크기로 표시되는데도 원본 이미지 전송
- **사용자 경험**: 이미지가 완전히 로드될 때까지 기다려야 함
- **모바일 환경**: 작은 화면에서 큰 이미지 처리 시 성능 저하

### 썸네일 시스템의 장점

- **빠른 로딩**: 작은 크기의 썸네일로 빠른 화면 표시
- **대역폭 절약**: 필요한 크기만큼만 이미지 전송
- **반응형 UI**: 다양한 화면 크기에 최적화된 이미지 제공
- **사용자 편의성**: 목록에서 이미지 내용을 한눈에 파악 가능

## 📚 이론 포인트 리마인드

### 1. 이미지 처리 원리

- **픽셀 기반**: 이미지는 픽셀의 2차원 배열로 구성
- **리샘플링**: 크기 변경 시 픽셀 정보를 재계산
- **압축**: JPEG, PNG 등 다양한 압축 알고리즘 사용

### 2. 썸네일 생성 방식

- **리사이징**: 원본 이미지 크기를 줄여서 썸네일 생성
- **크롭핑**: 원본 이미지의 일부를 잘라서 썸네일 생성
- **비율 유지**: 원본 이미지의 가로세로 비율을 유지하면서 크기 조정

### 3. 이미지 포맷별 특징

- **JPEG**: 사진에 적합, 압축률 높음, 투명도 미지원
- **PNG**: 투명도 지원, 무손실 압축, 파일 크기 큼
- **GIF**: 애니메이션 지원, 색상 수 제한

## 🚀 실습 단계별 진행

### 1단계: ThumbnailGenerator 유틸리티 클래스 생성

#### ThumbnailGenerator.java 생성

```java
package io.goorm.backend.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 이미지 썸네일 생성을 위한 유틸리티 클래스
 */
public class ThumbnailGenerator {

    // 기본 썸네일 크기들
    public static final int THUMBNAIL_SMALL = 150;   // 150x150
    public static final int THUMBNAIL_MEDIUM = 300;  // 300x300
    public static final int THUMBNAIL_LARGE = 500;   // 500x500

    /**
     * 이미지 파일에서 썸네일 생성
     *
     * @param sourcePath 원본 이미지 파일 경로
     * @param targetPath 썸네일 저장 경로
     * @param maxSize 썸네일의 최대 크기 (가로/세로)
     * @return 생성 성공 여부
     */
    public static boolean generateThumbnail(String sourcePath, String targetPath, int maxSize) {
        try {
            // 원본 이미지 로드
            BufferedImage sourceImage = ImageIO.read(new File(sourcePath));
            if (sourceImage == null) {
                return false;
            }

            // 썸네일 크기 계산 (비율 유지)
            Dimension thumbnailSize = calculateThumbnailSize(
                sourceImage.getWidth(),
                sourceImage.getHeight(),
                maxSize
            );

            // 썸네일 이미지 생성
            BufferedImage thumbnail = createThumbnail(sourceImage, thumbnailSize);

            // 썸네일 저장
            return saveThumbnail(thumbnail, targetPath);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 썸네일 크기 계산 (비율 유지)
     */
    private static Dimension calculateThumbnailSize(int originalWidth, int originalHeight, int maxSize) {
        double ratio = Math.min(
            (double) maxSize / originalWidth,
            (double) maxSize / originalHeight
        );

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        return new Dimension(newWidth, newHeight);
    }

    /**
     * 썸네일 이미지 생성
     */
    private static BufferedImage createThumbnail(BufferedImage source, Dimension size) {
        // 썸네일용 BufferedImage 생성
        BufferedImage thumbnail = new BufferedImage(
            size.width,
            size.height,
            BufferedImage.TYPE_INT_RGB
        );

        // Graphics2D 객체 생성 및 설정
        Graphics2D g2d = thumbnail.createGraphics();

        // 렌더링 품질 설정
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 썸네일 그리기
        g2d.drawImage(source, 0, 0, size.width, size.height, null);

        // Graphics2D 해제
        g2d.dispose();

        return thumbnail;
    }

    /**
     * 썸네일 저장
     */
    private static boolean saveThumbnail(BufferedImage thumbnail, String targetPath) {
        try {
            // 디렉토리 생성
            Path path = Paths.get(targetPath);
            Files.createDirectories(path.getParent());

            // 파일 확장자에 따른 포맷 결정
            String format = getImageFormat(targetPath);

            // 썸네일 저장
            return ImageIO.write(thumbnail, format, new File(targetPath));

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 파일 경로에서 이미지 포맷 추출
     */
    private static String getImageFormat(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();

        switch (extension) {
            case "jpg":
            case "jpeg":
                return "JPEG";
            case "png":
                return "PNG";
            case "gif":
                return "GIF";
            default:
                return "JPEG"; // 기본값
        }
    }

    /**
     * 여러 크기의 썸네일을 한 번에 생성
     */
    public static boolean generateMultipleThumbnails(String sourcePath, String baseDir, String fileName) {
        boolean success = true;

        // 파일명에서 확장자 제거
        String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf("."));
        String extension = fileName.substring(fileName.lastIndexOf("."));

        // 여러 크기의 썸네일 생성
        int[] sizes = {THUMBNAIL_SMALL, THUMBNAIL_MEDIUM, THUMBNAIL_LARGE};

        for (int size : sizes) {
            String thumbnailPath = baseDir + "/" + nameWithoutExt + "_" + size + extension;
            if (!generateThumbnail(sourcePath, thumbnailPath, size)) {
                success = false;
            }
        }

        return success;
    }

    /**
     * 썸네일 파일 존재 여부 확인
     */
    public static boolean thumbnailExists(String thumbnailPath) {
        return Files.exists(Paths.get(thumbnailPath));
    }

    /**
     * 썸네일 파일 삭제
     */
    public static boolean deleteThumbnail(String thumbnailPath) {
        try {
            return Files.deleteIfExists(Paths.get(thumbnailPath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
```

### 2단계: FileUpload 모델에 썸네일 정보 추가

#### FileUpload.java 수정

```java
// 기존 FileUpload 클래스에 썸네일 관련 필드 추가

public class FileUpload {
    // 기존 필드들...
            private Long id;
    private int boardId;
    private String originalName;
    private String storedName;
    private long fileSize;
    private String fileType;
    private Date uploadDate;

    // 썸네일 관련 필드 추가
    private String thumbnailSmallPath;   // 150x150 썸네일 경로
    private String thumbnailMediumPath;  // 300x300 썸네일 경로
    private String thumbnailLargePath;   // 500x500 썸네일 경로
    private boolean thumbnailGenerated;  // 썸네일 생성 완료 여부

    // 기존 getter/setter...

    // 썸네일 관련 getter/setter
    public String getThumbnailSmallPath() { return thumbnailSmallPath; }
    public void setThumbnailSmallPath(String thumbnailSmallPath) { this.thumbnailSmallPath = thumbnailSmallPath; }

    public String getThumbnailMediumPath() { return thumbnailMediumPath; }
    public void setThumbnailMediumPath(String thumbnailMediumPath) { this.thumbnailMediumPath = thumbnailMediumPath; }

    public String getThumbnailLargePath() { return thumbnailLargePath; }
    public void setThumbnailLargePath(String thumbnailLargePath) { this.thumbnailLargePath = thumbnailLargePath; }

    public boolean isThumbnailGenerated() { return thumbnailGenerated; }
    public void setThumbnailGenerated(boolean thumbnailGenerated) { this.thumbnailGenerated = thumbnailGenerated; }

    /**
     * 특정 크기의 썸네일 경로 반환
     */
    public String getThumbnailPath(int size) {
        switch (size) {
            case 150: return thumbnailSmallPath;
            case 300: return thumbnailMediumPath;
            case 500: return thumbnailLargePath;
            default: return thumbnailSmallPath;
        }
    }

    /**
     * 이미지 파일 여부 확인
     */
    public boolean isImageFile() {
        return fileType != null && fileType.startsWith("image/");
    }
}
```

### 3단계: FileUploadDAO에 썸네일 관련 메서드 추가

#### FileUploadDAO.java 수정

```java
// 기존 FileUploadDAO에 썸네일 관련 메서드 추가

/**
 * 썸네일 정보 업데이트
 */
public boolean updateThumbnailInfo(int fileId, String thumbnailSmallPath,
                                 String thumbnailMediumPath, String thumbnailLargePath) {
    String sql = "UPDATE file_upload SET " +
                 "thumbnail_small_path = ?, " +
                 "thumbnail_medium_path = ?, " +
                 "thumbnail_large_path = ?, " +
                 "thumbnail_generated = true " +
                 "WHERE id = ?";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, thumbnailSmallPath);
        pstmt.setString(2, thumbnailMediumPath);
        pstmt.setString(3, thumbnailLargePath);
        pstmt.setInt(4, fileId);

        return pstmt.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

/**
 * 썸네일 생성이 필요한 이미지 파일 목록 조회
 */
public List<FileUpload> getImageFilesWithoutThumbnail() {
    String sql = "SELECT * FROM file_upload " +
                 "WHERE file_type LIKE 'image/%' " +
                 "AND (thumbnail_generated = false OR thumbnail_generated IS NULL)";

    List<FileUpload> files = new ArrayList<>();

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
            FileUpload file = mapResultSetToFileUpload(rs);
            files.add(file);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return files;
}

/**
 * 게시글의 모든 이미지 파일 조회 (썸네일 포함)
 */
public List<FileUpload> getImageFilesWithThumbnails(int boardId) {
    String sql = "SELECT * FROM file_upload " +
                 "WHERE board_id = ? AND file_type LIKE 'image/%' " +
                 "ORDER BY upload_date DESC";

    List<FileUpload> files = new ArrayList<>();

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, boardId);

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                FileUpload file = mapResultSetToFileUpload(rs);
                files.add(file);
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return files;
}
```

### 4단계: FileUploadCommand에서 썸네일 자동 생성

#### FileUploadCommand.java 수정

```java
// 기존 FileUploadCommand의 파일 업로드 처리 부분에 썸네일 생성 로직 추가

/**
 * 파일 업로드 후 썸네일 생성
 */
private void generateThumbnailsForImage(FileUpload fileUpload, String uploadPath) {
    // 이미지 파일인 경우에만 썸네일 생성
    if (!fileUpload.isImageFile()) {
        return;
    }

    try {
        // 썸네일 저장 디렉토리 설정
        String thumbnailDir = uploadPath + "/thumbnails";

        // 원본 파일 경로
        String sourcePath = uploadPath + "/" + fileUpload.getStoredName();

        // 여러 크기의 썸네일 생성
        boolean success = ThumbnailGenerator.generateMultipleThumbnails(
            sourcePath,
            thumbnailDir,
            fileUpload.getStoredName()
        );

        if (success) {
            // 썸네일 경로 설정
            String nameWithoutExt = fileUpload.getStoredName().substring(0,
                fileUpload.getStoredName().lastIndexOf("."));
            String extension = fileUpload.getStoredName().substring(
                fileUpload.getStoredName().lastIndexOf("."));

            fileUpload.setThumbnailSmallPath("thumbnails/" + nameWithoutExt + "_150" + extension);
            fileUpload.setThumbnailMediumPath("thumbnails/" + nameWithoutExt + "_300" + extension);
            fileUpload.setThumbnailLargePath("thumbnails/" + nameWithoutExt + "_500" + extension);

            // 데이터베이스에 썸네일 정보 업데이트
            FileUploadDAO fileUploadDAO = new FileUploadDAO();
            fileUploadDAO.updateThumbnailInfo(
                fileUpload.getId(),
                fileUpload.getThumbnailSmallPath(),
                fileUpload.getThumbnailMediumPath(),
                fileUpload.getThumbnailLargePath()
            );
        }

    } catch (Exception e) {
        e.printStackTrace();
        // 썸네일 생성 실패 시에도 파일 업로드는 성공으로 처리
    }
}
```

### 5단계: JSP에서 썸네일 표시

#### list.jsp 수정 (게시글 목록에서 썸네일 표시)

```jsp
<!-- 게시글 목록 테이블에 썸네일 컬럼 추가 -->
<thead class="bg-gray-50">
    <tr>
        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">번호</th>
        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">썸네일</th>
        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">제목</th>
        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">작성자</th>
        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">첨부파일</th>
        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">작성일</th>
    </tr>
</thead>

<!-- 게시글 목록 행에 썸네일 표시 -->
<tbody class="bg-white divide-y divide-gray-200">
    <c:forEach var="board" items="${boards}">
        <tr class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${board.id}</td>

            <!-- 썸네일 표시 -->
            <td class="px-6 py-4 whitespace-nowrap">
                <c:if test="${not empty board.thumbnailPath}">
                    <img src="${board.thumbnailPath}"
                         alt="게시글 썸네일"
                         class="w-16 h-16 object-cover rounded-lg border border-gray-200 hover:scale-110 transition-transform cursor-pointer"
                         onclick="showImagePreview('${board.thumbnailPath}', '${board.title}')">
                </c:if>
            </td>

            <!-- 나머지 컬럼들... -->
        </tr>
    </c:forEach>
</tbody>
```

#### view.jsp 수정 (게시글 상세보기에서 썸네일 표시)

```jsp
<!-- 첨부파일 섹션에 썸네일 표시 추가 -->
<c:if test="${not empty board.files and board.files.size() > 0}">
    <div class="px-6 py-4 border-b border-gray-200 bg-blue-50">
        <h3 class="text-sm font-medium text-gray-900 mb-3 flex items-center">
            <svg class="w-4 h-4 mr-2 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"></path>
            </svg>
            첨부파일 (${board.files.size()}개)
        </h3>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            <c:forEach var="file" items="${board.files}">
                <div class="flex items-center p-3 bg-white rounded-lg border border-gray-200 hover:border-blue-300 transition-colors">

                    <!-- 이미지 파일인 경우 썸네일 표시 -->
                    <c:if test="${file.imageFile}">
                        <div class="flex-shrink-0 mr-3">
                            <c:choose>
                                <c:when test="${not empty file.thumbnailSmallPath}">
                                    <img src="${file.thumbnailSmallPath}"
                                         alt="${file.originalName}"
                                         class="w-12 h-12 object-cover rounded border border-gray-200 hover:scale-110 transition-transform cursor-pointer"
                                         onclick="showImagePreview('${file.thumbnailMediumPath}', '${file.originalName}')">
                                </c:when>
                                <c:otherwise>
                                    <!-- 썸네일이 없는 경우 기본 아이콘 -->
                                    <svg class="w-12 h-12 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                                        <path fill-rule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clip-rule="evenodd"></path>
                                    </svg>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>

                    <!-- 파일 정보 및 다운로드 버튼... -->
                </div>
            </c:forEach>
        </div>
    </div>
</c:if>

<!-- 이미지 미리보기 모달 -->
<div id="imagePreviewModal" class="fixed inset-0 bg-black bg-opacity-50 hidden z-50">
    <div class="flex items-center justify-center min-h-screen p-4">
        <div class="bg-white rounded-lg max-w-4xl max-h-full overflow-auto">
            <div class="p-4 border-b border-gray-200 flex justify-between items-center">
                <h3 id="imagePreviewTitle" class="text-lg font-medium text-gray-900"></h3>
                <button onclick="closeImagePreview()" class="text-gray-400 hover:text-gray-600">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                    </svg>
                </button>
            </div>
            <div class="p-4">
                <img id="imagePreviewImg" src="" alt="" class="max-w-full max-h-96 mx-auto">
            </div>
        </div>
    </div>
</div>

<script>
function showImagePreview(imagePath, title) {
    document.getElementById('imagePreviewImg').src = imagePath;
    document.getElementById('imagePreviewTitle').textContent = title;
    document.getElementById('imagePreviewModal').classList.remove('hidden');
}

function closeImagePreview() {
    document.getElementById('imagePreviewModal').classList.add('hidden');
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeImagePreview();
    }
});
</script>
```

## 📝 완료 체크리스트

- [ ] ThumbnailGenerator 유틸리티 클래스 생성
- [ ] FileUpload 모델에 썸네일 정보 필드 추가
- [ ] FileUploadDAO에 썸네일 관련 메서드 추가
- [ ] FileUploadCommand에서 이미지 업로드 시 썸네일 자동 생성
- [ ] list.jsp에서 게시글 목록에 썸네일 표시
- [ ] view.jsp에서 첨부파일 썸네일 및 미리보기 기능 구현
- [ ] 썸네일 생성 및 표시 테스트 완료

## ⚠️ 주의사항

### 1. 성능 고려사항

- **메모리 사용량**: 대용량 이미지 처리 시 메모리 부족 방지
- **비동기 처리**: 썸네일 생성을 백그라운드에서 처리
- **캐싱**: 생성된 썸네일의 재사용으로 중복 생성 방지

### 2. 파일 관리

- **디렉토리 구조**: 썸네일 파일의 체계적인 저장 및 관리
- **정리 작업**: 원본 파일 삭제 시 연관된 썸네일도 함께 삭제
- **백업**: 중요한 썸네일 파일의 백업 전략 수립

### 3. 사용자 경험

- **로딩 표시**: 썸네일 생성 중 사용자에게 피드백 제공
- **폴백 처리**: 썸네일 생성 실패 시 기본 아이콘 표시
- **반응형 이미지**: 다양한 화면 크기에 최적화된 썸네일 제공

## 🎯 테스트 방법

### 1. 기본 썸네일 생성 테스트

- 이미지 파일 업로드 후 썸네일 자동 생성 확인
- 다양한 크기의 썸네일 파일 생성 확인
- 썸네일 파일의 품질 및 크기 검증

### 2. UI 표시 테스트

- 게시글 목록에서 썸네일 표시 확인
- 게시글 상세보기에서 썸네일 및 미리보기 기능 확인
- 썸네일 클릭 시 이미지 미리보기 모달 동작 확인

### 3. 에러 처리 테스트

- 썸네일 생성 실패 시 기본 아이콘 표시 확인
- 지원하지 않는 이미지 포맷 처리 확인
- 대용량 이미지 처리 시 메모리 관리 확인

## 🔗 다음 단계

이미지 썸네일 생성 시스템 구현 완료 후:

1. **step03**: 대용량 파일 업로드 처리 (청크 업로드, 진행률 표시)
2. **step04**: 고급 검색 기능 구현 (통합 검색, 필터링, 정렬)

이제 효율적인 썸네일 시스템으로 이미지 로딩 성능을 크게 향상시킬 수 있습니다!
