package xyz.vcluster.cassiopeia.common.utils.poi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.vcluster.cassiopeia.common.core.domain.AjaxResult;

import java.io.InputStream;
import java.util.List;

/**
 * Excel相关处理
 *
 * @author cassiopeia
 */
public class ExcelUtil<T> {
    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);

    /**
     * 实体对象
     */
    public Class<T> clazz;

    public ExcelUtil(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * 对excel表单默认第一个索引名转换成list
     *
     * @param is 输入流
     * @return 转换后集合
     */
    public List<T> importExcel(InputStream is) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @param list      导出数据集合
     * @param sheetName 工作表的名称
     * @return 结果
     */
    public AjaxResult exportExcel(List<T> list, String sheetName) {
        throw new UnsupportedOperationException();
    }

    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @param sheetName 工作表的名称
     * @return 结果
     */
    public AjaxResult importTemplateExcel(String sheetName) {
        throw new UnsupportedOperationException();
    }
}
