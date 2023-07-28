package xyz.vcluster.cassiopeia.common.exception.file;

import xyz.vcluster.cassiopeia.common.exception.base.BaseException;

/**
 * 文件信息异常类
 *
 * @author cassiopeia
 */
public class FileException extends BaseException {
    private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args) {
        super("file", code, args, null);
    }

}
