package eu.linkedeodata.geotriples.writers.dimis;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.n3.N3JenaWriterPP;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

public class WP2N3JenaWriterPP extends N3JenaWriterPP {
	//public static Map<String, String> thiswellKnownPropsMap;
	public WP2N3JenaWriterPP()
	{
		super();
	}
	public WP2N3JenaWriterPP(boolean useWellKnownPropertySymbls,boolean allowTripleQuotedStrngs,Map<String, String> wellKnownPropsMapTrtl) {
		super();
		useWellKnownPropertySymbols = useWellKnownPropertySymbls;
		allowTripleQuotedStrings=allowTripleQuotedStrngs;
		wellKnownPropsMap = wellKnownPropsMapTrtl;
		//thiswellKnownPropsMap=wellKnownPropsMap;
	}
	public WP2N3JenaWriterPP(boolean allowTripleQuotedStrngs,Map<String, String> wellKnownPropsMapTrtl) {
		super();
		allowTripleQuotedStrings=allowTripleQuotedStrngs;
		wellKnownPropsMap = wellKnownPropsMapTrtl;
		//thiswellKnownPropsMap=wellKnownPropsMap;
	}
	public static Map<String, String> getWellKnownPropsMap()
	{
		//thiswellKnownPropsMap=new HashMap<String, String>();
		return wellKnownPropsMapN3;
		//return new HashMap<String,String>(wellKnownPropsMap );
	}
	@Override
	protected void writePropertiesForSubject(Resource subj, ClosableIterator<Property> iter)
    {
        // For each property.

		for (; iter.hasNext();)
        {
            Property property = iter.next();
            
            int size=countProperties(subj, property);
            if(size==0) //dimis
            {
            	continue;
            }

            // Object list
            writeObjectList(subj, property);
            
            
            if (iter.hasNext())
                out.println(" ;");
        }
        iter.close();
    }
	@Override
	protected int countProperties(Resource r, Property p) 
	{
		int numProp = 0 ;
		StmtIterator sIter = r.listProperties(p) ;
		for ( ; sIter.hasNext() ; )
		{
			Statement stmnt=sIter.nextStatement() ;
			if(! stmnt.getObject().toString().startsWith("null"))
			{
				numProp++ ;
			}
		}
		sIter.close() ;
		return numProp ;
	}
	@Override
    protected void writeObjectList(Resource subject, Property property)
    {
//        if ( ! doObjectListsAsLists )
//        {
//            super.writeObjectList(resource, property) ;
//            return ;
//        }

        String propStr = formatProperty(property);
        
        // Find which objects are simple (i.e. not nested structures)             

        StmtIterator sIter = subject.listProperties(property);
        Set<RDFNode> simple = new HashSet<RDFNode>() ;
        Set<RDFNode> complex = new HashSet<RDFNode>() ;

        for (; sIter.hasNext();)
        {
            Statement stmt = sIter.nextStatement();
            RDFNode obj = stmt.getObject() ;
            if(obj.toString().startsWith("null")) //dimis
            {
            	continue;
            }
            if ( isSimpleObject(obj) )
                simple.add(obj) ;
            else
                complex.add(obj) ;
        }
        sIter.close() ;
        // DEBUG
        @SuppressWarnings("unused")
		int simpleSize = simple.size() ;
        @SuppressWarnings("unused")
		int complexSize = complex.size() ;
        
        // Write property/simple objects
        
        if ( simple.size() > 0 )
        {
            String padSp = null ;
            // Simple objects - allow property to be long and alignment to be lost
            if ((propStr.length()+minGap) <= widePropertyLen)
                padSp = pad(calcPropertyPadding(propStr)) ;
            
            if ( doObjectListsAsLists )
            {
                // Write all simple objects as one list. 
                out.print(propStr);
                out.incIndent(indentObject) ; 
            
                if ( padSp != null )
                    out.print(padSp) ;
                else
                    out.println() ;
            
                for (Iterator<RDFNode> iter = simple.iterator(); iter.hasNext();)
                {
                    RDFNode n = iter.next();
                    writeObject(n);
                    
                    // As an object list
                    if (iter.hasNext())
                        out.print(objectListSep);
                }
                
                out.decIndent(indentObject) ;
            }
            else
            {
                for (Iterator<RDFNode> iter = simple.iterator(); iter.hasNext();)
                {
                    // This is also the same as the complex case 
                    // except the width the property can go in is different.
                    out.print(propStr);
                    out.incIndent(indentObject) ; 
                    if ( padSp != null )
                        out.print(padSp) ;
                    else
                        out.println() ;
                    
                    RDFNode n = iter.next();
                    writeObject(n);
                    out.decIndent(indentObject) ;
                    
                    // As an object list
                    if (iter.hasNext())
                        out.println(" ;");
                   }
                
            }
        }        
        // Now do complex objects.
        // Write property each time for a complex object.
        // Do not allow over long properties but same line objects.

        if (complex.size() > 0)
        {
            // Finish the simple list if there was one
            if ( simple.size() > 0 )
                out.println(" ;");
            
            int padding = -1 ;
            String padSp = null ;
            
            // Can we fit teh start of teh complex object on this line?
            
            // DEBUG variable.
            @SuppressWarnings("unused")
			int tmp = propStr.length() ;
            // Complex objects - do not allow property to be long and alignment to be lost
            if ((propStr.length()+minGap) <= propertyCol)
            {
                padding = calcPropertyPadding(propStr) ;
                padSp = pad(padding) ;
            }

            for (Iterator<RDFNode> iter = complex.iterator(); iter.hasNext();)
            {
                int thisIndent = indentObject ;
                //if ( i )
                out.incIndent(thisIndent);
                out.print(propStr);
                if ( padSp != null )
                    out.print(padSp) ;
                else
                    out.println() ;
            
                RDFNode n = iter.next();
                writeObject(n);
                out.decIndent(thisIndent);
                if ( iter.hasNext() )
                    out.println(" ;");
            }
        }
        return;
	}
}
