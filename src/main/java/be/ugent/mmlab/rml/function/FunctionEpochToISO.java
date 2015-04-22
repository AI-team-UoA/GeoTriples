package be.ugent.mmlab.rml.function;

import be.ugent.mmlab.rml.vocabulary.Vocab;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jgl@spaceapplications.com on 22/04/2015.
 */
public class FunctionEpochToISO extends AbstractFunction implements Function {
    Vocab.QLTerm termkind;
    public FunctionEpochToISO(Vocab.QLTerm termkind) {
        this.termkind=termkind;
    }
    @Override
    public List<? extends String> execute(List<? extends String> arguments) throws Exception {
        List<String> valueList = new ArrayList<>();

        String epoch = arguments.get(0);
        String isoDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date(Long.parseLong(epoch)));

        valueList.add(isoDate);
        return valueList;
    }
}
