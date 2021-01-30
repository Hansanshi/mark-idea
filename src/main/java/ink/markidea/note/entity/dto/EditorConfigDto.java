package ink.markidea.note.entity.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hansanshi
 * @date 2021/1/28
 */
@Getter
@Setter
public class EditorConfigDto {

    /**
     * 用户自定义样式路径
     */
    private String customStylePath;

    /**
     * 使用自定义css
     */
    private boolean enableCustomStyle = false;


    /**
     * 开启计数器
     */
    private boolean enableCounter = false;

    /**
     * 编辑模式
     * 支持sv, ir, wysiwyg
     */
    private String editMode = "ir";

    /**
     * 启用代码高亮
     */
    private boolean enableHighLight = true;


    /**
     * 代码块样式
     */
    private String codeStyle = "native";

    /**
     * 启用代码行号
     */
    private boolean enableLineNumber = false;

    /**
     * 大纲位置
     */
    private String outlinePosition = "left";

}
