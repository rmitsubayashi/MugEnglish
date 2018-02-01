package pelicann.linnca.com.corefunctionality.lessonscript;

import java.util.List;

import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;

public abstract class ScriptGenerator {
    public abstract Script makeScript(List<EntityPropertyData> data);
}
