package xyz.vcluster.cassiopeia.common.utils.file;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * 文件处理工具类
 *
 * @author cassiopeia
 */
public class LocalFileUtils extends FileUtils {

    /**
     * 读取指定文件
     *
     * @param fileName 文件名称
     * @return 文件数据
     * @throws IOException IO异常
     */
    public static byte[] read(String fileName) throws IOException {
        byte[] data = null;

        FileInputStream fis = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                throw new FileNotFoundException(fileName);
            }
            fis = new FileInputStream(file);
            long fileLength = file.length();
            data = fileLength > 0L ? IOUtils.toByteArray(fis, fileLength) : IOUtils.toByteArray(fis);
        } catch (IOException e) {
            throw e;
        } finally {
            IOUtils.close(fis);
        }

        return data;
    }

    /**
     * 写数据到文件中
     *
     * @param data     数据
     * @param fileName 目标文件
     * @return 目标文件
     * @throws IOException IO异常
     */
    public static String write(byte[] data, String fileName) throws IOException {
        FileOutputStream fos = null;
        try {
            File file = new File(fileName);

            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
            }
            fos = new FileOutputStream(file);
            fos.write(data);
        } finally {
            IOUtils.close(fos);
        }

        return fileName;
    }

    /**
     * 文件是否存在
     *
     * @param fileName 文件名称
     * @return 是否存在
     */
    public static boolean exists(String fileName) {

        return (new File(fileName)).exists();
    }

    /**
     * 获取子文件列表
     *
     * @param path 文件路径
     * @return 子文件列表
     */
    public static String[] list(String path) {
        String[] subFilName = {};
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                List<String> array = new LinkedList<>();
                for (File innerFile : files) {
                    array.add(innerFile.getName());
                }
                subFilName = array.toArray(new String[0]);
            }
        }

        return subFilName;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件
     * @return
     */
    public static boolean delete(String filePath) {
        boolean flag = false;
        File file = new File(filePath);
        // 路径为文件且不为空则进行删除
        if (file.exists()) {
            if (file.isFile()) {
                flag = file.delete();
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    flag = true;
                    for (File innerFile : files) {
                        flag = flag && delete(innerFile);
                    }
                }
                flag = flag && file.delete();
            }
        }
        return flag;
    }

    /**
     * 删除文件
     *
     * @param file 文件
     * @return
     */
    public static boolean delete(File file) {
        boolean flag = false;
        // 路径为文件且不为空则进行删除
        if (file.exists()) {
            if (file.isFile()) {
                flag = file.delete();
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    flag = true;
                    for (File innerFile : files) {
                        flag = flag && delete(innerFile);
                    }
                }
                flag = flag && file.delete();
            }
        }
        return flag;
    }
}
