/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package be.ugent.mmlab.rml.function;

import javax.xml.namespace.QName;

import org.geotools.gml2.FeatureTypeCache;
import org.geotools.gml2.SrsSyntax;
import org.geotools.gml2.bindings.GMLCoordTypeBinding;
import org.geotools.gml2.bindings.GMLCoordinatesTypeBinding;
import org.geotools.gml3.ArcParameters;
import org.geotools.gml3.XSDIdRegistry;
import org.geotools.gml3.bindings.AbstractFeatureCollectionTypeBinding;
import org.geotools.gml3.bindings.AbstractFeatureTypeBinding;
import org.geotools.gml3.bindings.AbstractGeometryTypeBinding;
import org.geotools.gml3.bindings.AbstractRingPropertyTypeBinding;
import org.geotools.gml3.bindings.ArcStringTypeBinding;
import org.geotools.gml3.bindings.ArcTypeBinding;
import org.geotools.gml3.bindings.BoundingShapeTypeBinding;
import org.geotools.gml3.bindings.CircleTypeBinding;
import org.geotools.gml3.bindings.ComplexSupportXSAnyTypeBinding;
import org.geotools.gml3.bindings.CurveArrayPropertyTypeBinding;
import org.geotools.gml3.bindings.CurvePropertyTypeBinding;
import org.geotools.gml3.bindings.CurveSegmentArrayPropertyTypeBinding;
import org.geotools.gml3.bindings.CurveTypeBinding;
import org.geotools.gml3.bindings.DirectPositionListTypeBinding;
import org.geotools.gml3.bindings.DirectPositionTypeBinding;
import org.geotools.gml3.bindings.DoubleListBinding;
import org.geotools.gml3.bindings.EnvelopeTypeBinding;
import org.geotools.gml3.bindings.FeatureArrayPropertyTypeBinding;
import org.geotools.gml3.bindings.FeaturePropertyTypeBinding;
import org.geotools.gml3.bindings.GML3EncodingUtils;
import org.geotools.gml3.bindings.GeometryPropertyTypeBinding;
import org.geotools.gml3.bindings.IntegerListBinding;
import org.geotools.gml3.bindings.LineStringPropertyTypeBinding;
import org.geotools.gml3.bindings.LineStringSegmentTypeBinding;
import org.geotools.gml3.bindings.LineStringTypeBinding;
import org.geotools.gml3.bindings.LinearRingPropertyTypeBinding;
import org.geotools.gml3.bindings.LinearRingTypeBinding;
import org.geotools.gml3.bindings.LocationPropertyTypeBinding;
import org.geotools.gml3.bindings.MeasureTypeBinding;
import org.geotools.gml3.bindings.MultiCurvePropertyTypeBinding;
import org.geotools.gml3.bindings.MultiCurveTypeBinding;
import org.geotools.gml3.bindings.MultiGeometryPropertyTypeBinding;
import org.geotools.gml3.bindings.MultiGeometryTypeBinding;
import org.geotools.gml3.bindings.MultiLineStringPropertyTypeBinding;
import org.geotools.gml3.bindings.MultiLineStringTypeBinding;
import org.geotools.gml3.bindings.MultiPointPropertyTypeBinding;
import org.geotools.gml3.bindings.MultiPointTypeBinding;
import org.geotools.gml3.bindings.MultiPolygonPropertyTypeBinding;
import org.geotools.gml3.bindings.MultiPolygonTypeBinding;
import org.geotools.gml3.bindings.MultiSurfacePropertyTypeBinding;
import org.geotools.gml3.bindings.MultiSurfaceTypeBinding;
import org.geotools.gml3.bindings.NullTypeBinding;
import org.geotools.gml3.bindings.PointArrayPropertyTypeBinding;
import org.geotools.gml3.bindings.PointPropertyTypeBinding;
import org.geotools.gml3.bindings.PointTypeBinding;
import org.geotools.gml3.bindings.PolygonPatchTypeBinding;
import org.geotools.gml3.bindings.PolygonPropertyTypeBinding;
import org.geotools.gml3.bindings.PolygonTypeBinding;
import org.geotools.gml3.bindings.ReferenceTypeBinding;
import org.geotools.gml3.bindings.RingTypeBinding;
import org.geotools.gml3.bindings.SurfaceArrayPropertyTypeBinding;
import org.geotools.gml3.bindings.SurfacePatchArrayPropertyTypeBinding;
import org.geotools.gml3.bindings.SurfacePropertyTypeBinding;
import org.geotools.gml3.bindings.SurfaceTypeBinding;
import org.geotools.gml3.bindings.TimeInstantPropertyTypeBinding;
import org.geotools.gml3.bindings.TimeInstantTypeBinding;
import org.geotools.gml3.bindings.TimePeriodTypeBinding;
import org.geotools.gml3.bindings.TimePositionTypeBinding;
import org.geotools.gml3.bindings.TimePositionUnionBinding;
import org.geotools.gml3.bindings.ext.CompositeCurveTypeBinding;
import org.geotools.gml3.smil.SMIL20Configuration;
import org.geotools.gml3.smil.SMIL20LANGConfiguration;
import org.geotools.xlink.XLINKConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.geotools.xs.XS;
import org.picocontainer.MutablePicoContainer;

import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;


/**
 * Parser configuration for the gml3 schema.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 *
 *
 *
 * @source $URL$
 */
public class GMLConfiguration extends Configuration {
    
    /**
     * Boolean property which controls whether encoded features should include bounds.
     */
    public static final QName NO_FEATURE_BOUNDS = org.geotools.gml2.GMLConfiguration.NO_FEATURE_BOUNDS;
    
    /**
     * Boolean property which controls whether the FeatureCollection should be encoded with multiple featureMember
     * as opposed to a single featureMembers 
     */
    public static final QName ENCODE_FEATURE_MEMBER = org.geotools.gml2.GMLConfiguration.ENCODE_FEATURE_MEMBER;

    /**
     * Boolean property which controls whether geometry and envelope objects are encoded with an 
     * srs dimension attribute.
     */
    public static final QName NO_SRS_DIMENSION = new QName( "org.geotools.gml", "noSrsDimension" );

    /**
     * extended support for arcs and surface flag
     */
    boolean extArcSurfaceSupport = false;

    /**
     * Srs name style to encode srsName URI's with
     */
    protected SrsSyntax srsSyntax = SrsSyntax.OGC_URN_EXPERIMENTAL;

    public GMLConfiguration() {
        this(false);
    }
    
    public GMLConfiguration(boolean extArcSurfaceSupport) {
        super(GMLd.getInstance());

        this.extArcSurfaceSupport = extArcSurfaceSupport;
        
        //add xlink cdependency
        addDependency(new XLINKConfiguration());

        //add smil depenedncy
        addDependency(new SMIL20Configuration());
        addDependency(new SMIL20LANGConfiguration());

        //add parser properties
        getProperties().add(Parser.Properties.PARSE_UNKNOWN_ELEMENTS);
        getProperties().add(Parser.Properties.PARSE_UNKNOWN_ATTRIBUTES);
    }

    /**
     * Sets the syntax to use for encoding srs uris.
     * <p>
     * If this method is not explicitly called {@link SrsSyntax#URN} is used as the default.
     * </p>
     */
    public void setSrsSyntax(SrsSyntax srsSyntax) {
        this.srsSyntax = srsSyntax;
    }

    /**
     * Returns the syntax to use for encoding srs uris.
     */
    public SrsSyntax getSrsSyntax() {
        return srsSyntax;
    }

    /**
     * Flag that when set triggers extended support for arcs and surfaces.
     */
    public void setExtendedArcSurfaceSupport(boolean arcSurfaceSupport) {
        this.extArcSurfaceSupport = arcSurfaceSupport;
    }
    
    public boolean isExtendedArcSurfaceSupport() {
        return extArcSurfaceSupport;
    }
    
    protected void registerBindings(MutablePicoContainer container) {
        //Types
        container.registerComponentImplementation(GMLd.AbstractFeatureType,
            AbstractFeatureTypeBinding.class);
        container.registerComponentImplementation(GMLd.AbstractFeatureCollectionType,
            AbstractFeatureCollectionTypeBinding.class);
        container.registerComponentImplementation(GMLd.AbstractGeometryType,
            AbstractGeometryTypeBinding.class);
        container.registerComponentImplementation(GMLd.AbstractRingPropertyType,
            AbstractRingPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.BoundingShapeType,
            BoundingShapeTypeBinding.class);
        //container.registerComponentImplementation(GMLd.COORDINATESTYPE,CoordinatesTypeBinding.class);
        container.registerComponentImplementation(GMLd.CoordinatesType,
            GMLCoordinatesTypeBinding.class);
        //container.registerComponentImplementation(GMLd.COORDTYPE,CoordTypeBinding.class);
        container.registerComponentImplementation(GMLd.CoordType, GMLCoordTypeBinding.class);
        container.registerComponentImplementation(GMLd.CurveArrayPropertyType,
            CurveArrayPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.CurveType, CurveTypeBinding.class);
        container.registerComponentImplementation(GMLd.CurvePropertyType,
            CurvePropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.CurveSegmentArrayPropertyType,
            CurveSegmentArrayPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.DirectPositionListType,
            DirectPositionListTypeBinding.class);
        container.registerComponentImplementation(GMLd.DirectPositionType,
            DirectPositionTypeBinding.class);
        container.registerComponentImplementation(GMLd.doubleList, DoubleListBinding.class);
        container.registerComponentImplementation(GMLd.EnvelopeType, EnvelopeTypeBinding.class);
        container.registerComponentImplementation(GMLd.FeatureArrayPropertyType,
            FeatureArrayPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.FeaturePropertyType,
            FeaturePropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.GeometryPropertyType,
            GeometryPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.integerList, IntegerListBinding.class);
        container.registerComponentImplementation(GMLd.LinearRingPropertyType,
            LinearRingPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.LinearRingType, LinearRingTypeBinding.class);
        container.registerComponentImplementation(GMLd.LineStringPropertyType,
            LineStringPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.LineStringSegmentType,
            LineStringSegmentTypeBinding.class);
        container.registerComponentImplementation(GMLd.LineStringType, LineStringTypeBinding.class);
        container.registerComponentImplementation(GMLd.LocationPropertyType,
            LocationPropertyTypeBinding.class);

        container.registerComponentImplementation(GMLd.MeasureType, MeasureTypeBinding.class);
        container.registerComponentImplementation(GMLd.MultiCurveType, MultiCurveTypeBinding.class);
        container.registerComponentImplementation(GMLd.MultiCurvePropertyType,
            MultiCurvePropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.MultiGeometryType, MultiGeometryTypeBinding.class);
        container.registerComponentImplementation(GMLd.MultiGeometryPropertyType, MultiGeometryPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.MultiLineStringPropertyType,
            MultiLineStringPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.MultiLineStringType,
            MultiLineStringTypeBinding.class);
        container.registerComponentImplementation(GMLd.MultiPointPropertyType,
            MultiPointPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.MultiPointType, MultiPointTypeBinding.class);
        container.registerComponentImplementation(GMLd.MultiPolygonPropertyType,
            MultiPolygonPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.MultiPolygonType,
            MultiPolygonTypeBinding.class);
        container.registerComponentImplementation(GMLd.MultiSurfaceType,
            MultiSurfaceTypeBinding.class);
        container.registerComponentImplementation(GMLd.MultiSurfacePropertyType,
            MultiSurfacePropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.NullType,
                NullTypeBinding.class);
        container.registerComponentImplementation(GMLd.PointArrayPropertyType,
            PointArrayPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.PointPropertyType,
            PointPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.PointType, PointTypeBinding.class);
        container.registerComponentImplementation(GMLd.PolygonPatchType,
                PolygonPatchTypeBinding.class);
        container.registerComponentImplementation(GMLd.PolygonPropertyType,
            PolygonPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.PolygonType, PolygonTypeBinding.class);
        container.registerComponentImplementation(GMLd.ReferenceType, ReferenceTypeBinding.class);
        container.registerComponentImplementation(GMLd.SurfaceArrayPropertyType,
            SurfaceArrayPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.SurfacePropertyType,
            SurfacePropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.SurfaceType, SurfaceTypeBinding.class);
        
        container.registerComponentImplementation(GMLd.TimeInstantType, TimeInstantTypeBinding.class);
        container.registerComponentImplementation(GMLd.TimeInstantPropertyType, TimeInstantPropertyTypeBinding.class);
        container.registerComponentImplementation(GMLd.TimePeriodType, TimePeriodTypeBinding.class);
        container.registerComponentImplementation(GMLd.TimePositionType, TimePositionTypeBinding.class);
        container.registerComponentImplementation(GMLd.TimePositionUnion, TimePositionUnionBinding.class);
        
        container.registerComponentImplementation(XS.ANYTYPE, ComplexSupportXSAnyTypeBinding.class);
        
        //extended bindings for arc/surface support
        if (isExtendedArcSurfaceSupport()) {
            container.registerComponentImplementation(GMLd.ArcStringType,
                    ArcStringTypeBinding.class);
            container.registerComponentImplementation(GMLd.ArcType,
                    ArcTypeBinding.class);
            container.registerComponentImplementation(GMLd.CircleType,
                    CircleTypeBinding.class);
            container.registerComponentImplementation(GMLd.RingType, RingTypeBinding.class);
            container.registerComponentImplementation(GMLd.SurfacePatchArrayPropertyType,
                    SurfacePatchArrayPropertyTypeBinding.class);
            container.registerComponentImplementation(GMLd.CompositeCurveType, 
                    CompositeCurveTypeBinding.class);
            container.registerComponentImplementation(GMLd.CurveArrayPropertyType, 
                    org.geotools.gml3.bindings.ext.CurveArrayPropertyTypeBinding.class);
            container.registerComponentImplementation(GMLd.CurvePropertyType, 
                    org.geotools.gml3.bindings.ext.CurvePropertyTypeBinding.class);
            container.registerComponentImplementation(GMLd.CurveType, 
                    org.geotools.gml3.bindings.ext.CurveTypeBinding.class);
            container.registerComponentImplementation(GMLd.MultiCurveType, 
                    org.geotools.gml3.bindings.ext.MultiCurveTypeBinding.class);
            container.registerComponentImplementation(GMLd.MultiPolygonType, 
                    org.geotools.gml3.bindings.ext.MultiPolygonTypeBinding.class);
            container.registerComponentImplementation(GMLd.MultiSurfaceType, 
                    org.geotools.gml3.bindings.ext.MultiSurfaceTypeBinding.class);
            container.registerComponentImplementation(GMLd.PolygonPatchType, 
                    org.geotools.gml3.bindings.ext.PolygonPatchTypeBinding.class);
            container.registerComponentImplementation(GMLd.SurfaceArrayPropertyType, 
                    org.geotools.gml3.bindings.ext.SurfaceArrayPropertyTypeBinding.class);
            container.registerComponentImplementation(GMLd.SurfacePatchArrayPropertyType, 
                    org.geotools.gml3.bindings.ext.SurfacePatchArrayPropertyTypeBinding.class);
            container.registerComponentImplementation(GMLd.SurfacePropertyType, 
                    org.geotools.gml3.bindings.ext.SurfacePropertyTypeBinding.class);
            container.registerComponentImplementation(GMLd.SurfaceType, 
                    org.geotools.gml3.bindings.ext.SurfaceTypeBinding.class);
        }
    }

    /**
     * Configures the gml3 context.
     * <p>
     * The following factories are registered:
     * <ul>
     * <li>{@link CoordinateArraySequenceFactory} under {@link CoordinateSequenceFactory}
     * <li>{@link GeometryFactory}
     * </ul>
     * </p>
     */
    public void configureContext(MutablePicoContainer container) {
        super.configureContext(container);

        container.registerComponentInstance(new FeatureTypeCache());
        container.registerComponentInstance(new XSDIdRegistry());

        //factories
        container.registerComponentInstance(CoordinateSequenceFactory.class,
            CoordinateArraySequenceFactory.instance());
        container.registerComponentImplementation(GeometryFactory.class);
        
        container.registerComponentInstance(new GML3EncodingUtils());
        
        if (isExtendedArcSurfaceSupport()) {
            container.registerComponentInstance(new ArcParameters());
        }

        container.registerComponentInstance(srsSyntax);
    }
}
