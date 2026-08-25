package com.ceramiflow.domain;

public enum StageType {
  FORMING, DRYING_REPAIR, PAINTING, GLAZING, READY_FOR_KILN, FIRING, QC, PACKAGING, COMPLETED;

  public StageType next() {
    return switch (this) {
      case FORMING -> DRYING_REPAIR;
      case DRYING_REPAIR -> PAINTING;
      case PAINTING -> GLAZING;
      case GLAZING -> READY_FOR_KILN;
      case READY_FOR_KILN -> FIRING;
      case FIRING -> QC;
      case QC -> PACKAGING;
      case PACKAGING -> COMPLETED;
      case COMPLETED -> COMPLETED;
    };
  }
}