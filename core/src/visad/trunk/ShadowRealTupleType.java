
//
// ShadowRealTupleType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

import java.util.*;
import java.rmi.*;

/**
   The ShadowRealTupleType class shadows the RealTupleType class,
   within a DataDisplayLink.<P>
*/
public class ShadowRealTupleType extends ShadowTupleType {

  // Shadow of Type.getCoordinateSystem().getReference()
  private ShadowRealTupleType Reference;

  // If allSpatial, need complex logic with permutation
  // need to indicate subspace of DisplaySpatialCartesianTuple
  // or of another spatial DisplayTupleType, and permutation
  // of subspace components
  //
  // true if all tupleComponents map to DisplaySpatialTuple
  // without overlap, or if allSpatialReference is true
  private boolean allSpatial;

  // true if using Reference.allSpatial
  private boolean allSpatialReference;

  // unique spatial DisplayTupleType whose components
  // are mapped from components of this
  // uniqueness enforced by BadMappingException-s
  //
  // implicit anySpatial = (DisplaySpatialTuple != null)
  private DisplayTupleType DisplaySpatialTuple;

  // mapping from components of DisplaySpatialTuple to
  // components of this, or -1
  // use default values for components of DisplaySpatial
  // whose permutation == -1 from ConstantMap-s or from
  // DisplayRealType.DefaultValue
  private int[] permutation;
 
  ShadowRealTupleType(MathType t, DataDisplayLink link, ShadowType parent)
      throws VisADException, RemoteException {
    super(t, link, parent);

    // check if tupleComponents are mapped to components of
    // Display.DisplaySpatialCartesianTuple, or to components
    // of a DisplayTupleType whose Reference is
    // Display.DisplaySpatialCartesianTuple
    allSpatial = true;
    allSpatialReference = false;
    DisplaySpatialTuple = null;

    // note DisplayIndices already computed by super constructor,
    // except for contribution by Reference
    for (int j=0; j<tupleComponents.length; j++) {
      ShadowRealType real = (ShadowRealType) tupleComponents[j];
      MappedDisplayScalar |= real.getMappedDisplayScalar();

      DisplayTupleType tuple = real.getDisplaySpatialTuple();
      int[] index = real.getDisplaySpatialTupleIndex();
      if (tuple != null) {
        if (DisplaySpatialTuple != null) {
          if (tuple.equals(DisplaySpatialTuple)) {
            for (int k=0; k<3 && index[k]>=0; k++) {
              if (permutation[index[k]] >= 0) {
                allSpatial = false;
              }
              else {
                permutation[index[k]] = j;
              }
            }
          }
          else {
            throw new BadMappingException("ShadowRealTupleType: mapped to multiple" +
                     " spatial DisplayTupleType-s");
          }
        }
        else { // DisplaySpatialTuple == null
          DisplaySpatialTuple = tuple;
          permutation = new int[tuple.getDimension()];
          for (int i=0; i<tuple.getDimension(); i++) permutation[i] = -1;
          for (int k=0; k<3 && index[k]>=0; k++) {
            permutation[index[k]] = j;
          }
        }
      }
      else { // tuple == null
        allSpatial = false;
      }
    } // end for (int j=0; j<tupleComponents.length; j++) {

    if (((RealTupleType) Type).getCoordinateSystem() != null) {
      RealTupleType ref =
        ((RealTupleType) Type).getCoordinateSystem().getReference();
      Reference = (ShadowRealTupleType) ref.buildShadowType(Link, this);
      DisplayTupleType tuple = Reference.getDisplaySpatialTuple();
      // note mappings of CoordinateSystem.Reference count
      DisplayIndices = addIndices(DisplayIndices, Reference.getDisplayIndices());
      ValueIndices = addIndices(ValueIndices, Reference.getValueIndices());
      if (tuple != null) {
        if (DisplaySpatialTuple != null) {
          if (!DisplaySpatialTuple.equals(tuple)) {
            throw new BadMappingException("ShadowRealTupleType: mapped to " +
            "multiple spatial DisplayTupleType-s (through " +
            " CoordinateSystem.Reference)");
          }
          else {
            allSpatial = false;
          }
        }
        else {
          DisplaySpatialTuple = tuple;
          allSpatial = Reference.getAllSpatial();
          allSpatialReference = allSpatial;
          permutation = Reference.getPermutation();
        }
      }
      MultipleDisplayScalar |=
        (MappedDisplayScalar && Reference.getMappedDisplayScalar());
      MappedDisplayScalar |= Reference.getMappedDisplayScalar();
    }
    else { // ((RealTupleType) Type).DefaultCoordinateSystem == null
      Reference = null;
    }
  }

  //
  // NOTE
  //
  // allow for any Flat ShadowTupleType or ShadowRealType
  //
  //
  void checkDirect() {
    if (LevelOfDifficulty == SIMPLE_TUPLE &&
        !MultipleDisplayScalar) {

      if (DisplaySpatialTuple.equals(
          Display.DisplaySpatialCartesianTuple)) {
        // this is mapped to a spatial DisplayRealType
        // (i.e., DisplaySpatialTuple != null)
        // and is not mapped through any (Display) CoordinateSystem
        // (i.e., DisplaySpatialTuple.equals(DisplaySpatialCartesianTuple) )
        isDirectManipulation = true;
      }
    }
  }

  public int[] getPermutation() {
    int[] ii = new int[permutation.length];
    for (int i=0; i<permutation.length; i++) ii[i] = permutation[i];
    return ii;
  }

  public boolean getAllSpatial() {
    return allSpatial;
  }

  public DisplayTupleType getDisplaySpatialTuple() {
    return DisplaySpatialTuple;
  }

  public boolean getMappedDisplayScalar() {
    return MappedDisplayScalar;
  }

  public ShadowRealTupleType getReference() {
    return Reference;
  }

}

