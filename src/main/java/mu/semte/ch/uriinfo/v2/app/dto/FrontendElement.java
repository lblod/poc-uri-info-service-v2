package mu.semte.ch.uriinfo.v2.app.dto;

public interface FrontendElement extends Comparable<FrontendElement>{
  ElementType getType();
  int getOrdering();

  default int compareTo(FrontendElement o) {
    return Integer.compare(getOrdering(), o.getOrdering());
  }
}
