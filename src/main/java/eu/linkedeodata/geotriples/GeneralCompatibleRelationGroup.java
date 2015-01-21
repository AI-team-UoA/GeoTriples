package eu.linkedeodata.geotriples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.d2rq.db.op.DatabaseOp;
import org.d2rq.nodes.BindingMaker;

public class GeneralCompatibleRelationGroup {

	public static Collection<GeneralCompatibleRelationGroup> groupNodeRelations(List<? extends GeneralNodeRelation> relations) {
		Collection<GeneralCompatibleRelationGroup> result = new ArrayList<GeneralCompatibleRelationGroup>();
		for (GeneralNodeRelation relation: relations) {
			result.add(new GeneralCompatibleRelationGroup(relation));
		}
		return result;
	}
	
	private final GeneralNodeRelation relation;
	
	public GeneralCompatibleRelationGroup(GeneralNodeRelation relation) {
		this.relation = relation;
	}
	
	public GeneralConnection getConnection() {
		return relation.getConnection();
	}
	
	public DatabaseOp baseRelation() {
		return relation.getBaseTabular();
	}
	
	public Collection<BindingMaker> bindingMakers() {
		return Collections.singleton(relation.getBindingMaker());
	}
}
