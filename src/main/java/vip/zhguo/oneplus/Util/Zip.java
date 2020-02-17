package vip.zhguo.oneplus.Util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.zip.*;

@Component
public class Zip {
    /**
     * 压缩
     * @param sourceFolder 指定打包的源目录
     * @param tarGzPath 指定目标 tar 包的位置
     * @return
     * @throws IOException
     */
    public static void compress(String sourceFolder, String tarGzPath) throws IOException {
        createTarFile(sourceFolder, tarGzPath);
    }

    private static void createTarFile(String sourceFolder, String tarGzPath) {
        TarArchiveOutputStream tarOs = null;
        try {
            // 创建一个 FileOutputStream 到输出文件（.tar.gz）
            FileOutputStream fos = new FileOutputStream(tarGzPath);
            // 创建一个 GZIPOutputStream，用来包装 FileOutputStream 对象
            GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
            // 创建一个 TarArchiveOutputStream，用来包装 GZIPOutputStream 对象
            tarOs = new TarArchiveOutputStream(gos);
            // 若不设置此模式，当文件名超过 100 个字节时会抛出异常，异常大致如下：
            // is too long ( > 100 bytes)
            // 具体可参考官方文档： http://commons.apache.org/proper/commons-compress/tar.html#Long_File_Names
            tarOs.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            addFilesToTarGZ(sourceFolder, "", tarOs);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                tarOs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void addFilesToTarGZ(String filePath, String parent, TarArchiveOutputStream tarArchive) throws IOException {
        File file = new File(filePath);
        // Create entry name relative to parent file path
        String entryName = parent + file.getName();
        // 添加 tar ArchiveEntry
        tarArchive.putArchiveEntry(new TarArchiveEntry(file, entryName));
        if (file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            // 写入文件
            IOUtils.copy(bis, tarArchive);
            tarArchive.closeArchiveEntry();
            bis.close();
        } else if (file.isDirectory()) {
            // 因为是个文件夹，无需写入内容，关闭即可
            tarArchive.closeArchiveEntry();
            // 读取文件夹下所有文件
            for (File f : file.listFiles()) {
                // 递归
                addFilesToTarGZ(f.getAbsolutePath(), entryName + File.separator, tarArchive);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // 测试一波，将 filebeat-7.1.0-linux-x86_64 打包成名为 filebeat-7.1.0-linux-x86_64.tar.gz 的 tar 包
        compress("D:\\down\\ce970714-2607-4e42-9413-26aef45c9e28", "D:\\down\\ce970714-2607-4e42-9413-26aef45c9e28.tar.gz");
    }

}