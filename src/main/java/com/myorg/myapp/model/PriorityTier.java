package com.myorg.myapp.model;

public enum PriorityTier {
  EMERGENCY,
  VIP,
  SCHEDULED,
  FERRY;

  public int rank() {
    return switch (this) {
      case EMERGENCY -> 0;
      case VIP -> 1;
      case SCHEDULED -> 2;
      case FERRY -> 3;
    };
  }
}
