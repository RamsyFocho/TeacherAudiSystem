package com.TeacherReportSystem.Ramsy.Services.TeacherModule;

import com.TeacherReportSystem.Ramsy.Model.TeacherModule.Teacher;
import com.TeacherReportSystem.Ramsy.Repositories.TeacherModule.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
@Service
@RequiredArgsConstructor
public class TeacherService {
    // This service can contain methods related to teacher operations and
//  collecting informations via excell sheets

    @Autowired
    TeacherRepository teacherRepo;

    public void uploadTeachersFromExcel(MultipartFile file) {
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip header

                String teacherId = getCellValue(row.getCell(0));
                String firstName = getCellValue(row.getCell(1));
                String lastName = getCellValue(row.getCell(2));
                String email = getCellValue(row.getCell(3));
                String phone = getCellValue(row.getCell(4));
                String gender = getCellValue(row.getCell(5));
//                String school = getCellValue(row.getCell(6));

                Optional<Teacher> existing = teacherRepo.findByEmail(email);
                Teacher teacher = existing.orElseGet(Teacher::new);

                teacher.setTeacherId(teacherId);
                teacher.setFirstName(firstName);
                teacher.setLastName(lastName);
                teacher.setEmail(email);
                teacher.setPhone(phone);
                teacher.setGender(gender);
//                teacher.setSchool(school);

                teacherRepo.save(teacher);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload Excel file: " + e.getMessage());
        }
    }
    private String getCellValue(Cell cell) {
        return cell != null ? cell.toString().trim() : "";
    }

}