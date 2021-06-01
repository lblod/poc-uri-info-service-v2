package mu.semte.ch.uriinfo.v2.app.dto.form;


public interface Input extends Comparable<Input>{
  int getOrdering();
  String getLabel();
  InputType getType();
  String getValue();

  default int compareTo(Input o) {
    return Integer.compare(getOrdering(), o.getOrdering());
  }

}
