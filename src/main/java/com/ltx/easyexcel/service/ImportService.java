package com.ltx.easyexcel.service;

import com.alibaba.excel.EasyExcel;
import com.ltx.entity.po.User;
import com.ltx.exception.CustomException;
import com.ltx.listener.UserListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author tianxing
 */
@Service
@Slf4j
public class ImportService {

    /**
     * 使用poi库导入
     * HSSFWorkbook -> .xls
     * XSSFWorkbook -> .xlsx
     * SXSSFWorkbook/DeferredSXSSFWorkbook -> 大型.xlsx
     *
     * @param file 文件
     * @return 用户列表
     */
    public List<User> importByPoi(MultipartFile file) {
        List<User> userList = new ArrayList<>();
        // 数据格式化
        DataFormatter dataFormatter = new DataFormatter();
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            // 最后一行
            int lastRowNum = sheet.getLastRowNum();
            // 跳过第一行(表头),rowNum=0
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    User user = new User();
                    user.setId((int) row.getCell(0).getNumericCellValue());
                    user.setName(row.getCell(1).getStringCellValue());
                    user.setPassword(Collections.singletonList(dataFormatter.formatCellValue(row.getCell(2))));
                    // 数字 -> 字符串
                    userList.add(user);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new CustomException(500, e.getMessage());
        }
        return userList;
    }

    /**
     * 使用easyExcel库导入
     *
     * @param file 文件
     * @return 用户列表
     */
    public List<User> importByEasyExcel(MultipartFile file) {
        UserListener userListener = new UserListener();
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
            // 默认Excel的第一行是头信息(表头) sheet从0开始 sheet()默认为第一页 doReadAll()读取全部数据
            EasyExcel.read(inputStream, User.class, userListener).sheet().doRead();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new CustomException(500, e.getMessage());
        }
        return userListener.getUserList();
    }
}
