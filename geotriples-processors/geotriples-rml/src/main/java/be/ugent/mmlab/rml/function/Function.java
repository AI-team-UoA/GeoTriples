package be.ugent.mmlab.rml.function;

import java.util.List;

import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

public interface Function {

	public List<? extends Object> execute(List<? extends Object> list,List<? extends QLTerm> qlterms) throws Exception;
}
