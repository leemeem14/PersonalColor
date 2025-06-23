package kr.ac.kopo.lyh.personalcolor.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.lyh.personalcolor.entity.ColorAnalysis;
import kr.ac.kopo.lyh.personalcolor.entity.User;
import kr.ac.kopo.lyh.personalcolor.service.ColorAnalysisService;
import kr.ac.kopo.lyh.personalcolor.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private final FileStorageService fileStorageService;
    private final ColorAnalysisService colorAnalysisService;

//    @GetMapping("/upload")
//    public String uploadForm() {
//        return "upload";
//    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        HttpServletRequest request) {
        try {
            // 로그인 확인
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "error", "로그인이 필요합니다."));
            }

            User user = (User) session.getAttribute("user");

            // 파일 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "파일이 비어있습니다."));
            }

            // 이미지 파일 확인
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "이미지 파일만 업로드 가능합니다."));
            }

            // 파일 저장
            String storedFileName = fileStorageService.storeFile(file);

            // 이미지 분석 수행
            ColorAnalysis analysis = colorAnalysisService.analyzeImage(
                    user,
                    file.getOriginalFilename(),
                    storedFileName
            );

            // 세션에 분석 결과 ID 저장
            session.setAttribute("latestAnalysisId", analysis.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "분석이 완료되었습니다!",
                    "analysisId", analysis.getId(),
                    "redirectUrl", "/results"
            ));

        } catch (Exception e) {
            log.error("파일 업로드 및 분석 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "분석 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/results")
    public String results(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        Long analysisId = (Long) session.getAttribute("latestAnalysisId");

        ColorAnalysis analysis;
        if (analysisId != null) {
            // 특정 분석 결과 조회
            analysis = colorAnalysisService.getUserAnalyses(user).stream()
                    .filter(a -> a.getId().equals(analysisId))
                    .findFirst()
                    .orElse(null);
        } else {
            // 최근 분석 결과 조회
            analysis = colorAnalysisService.getLatestAnalysis(user);
        }

        if (analysis == null) {
            return "redirect:/upload";
        }

        model.addAttribute("analysis", analysis);
        model.addAttribute("colorType", analysis.getColorType().getDisplayName());
        model.addAttribute("description", analysis.getDescription());
        BigDecimal confidence = analysis.getConfidence().multiply(BigDecimal.valueOf(100));
        model.addAttribute("confidence", confidence.setScale(0, RoundingMode.HALF_UP).intValue());
        model.addAttribute("uploadedFile", analysis.getStoredFileName());

        return "results";
    }

    @GetMapping("/history")
    public String history(HttpServletRequest request,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        Pageable pageable = PageRequest.of(page, size);
        Page<ColorAnalysis> analyses = colorAnalysisService.getUserAnalyses(user, pageable);

        model.addAttribute("analyses", analyses);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", analyses.getTotalPages());
        model.addAttribute("totalElements", analyses.getTotalElements());

        return "history";
    }

    @DeleteMapping("/analysis/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteAnalysis(@PathVariable Long id, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "error", "로그인이 필요합니다."));
            }

            User user = (User) session.getAttribute("user");
            colorAnalysisService.deleteAnalysis(id, user);

            return ResponseEntity.ok(Map.of("success", true, "message", "분석 결과가 삭제되었습니다."));

        } catch (Exception e) {
            log.error("분석 결과 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
