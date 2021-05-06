alias Acl.Accessibility.Always, as: AlwaysAccessible
alias Acl.GraphSpec.Constraint.Resource, as: ResourceConstraint
alias Acl.GraphSpec.Constraint.ResourceFormat, as: ResourceFormatConstraint
alias Acl.Accessibility.ByQuery, as: AccessByQuery
alias Acl.GraphSpec, as: GraphSpec
alias Acl.GroupSpec, as: GroupSpec
alias Acl.GroupSpec.GraphCleanup, as: GraphCleanup

defmodule Acl.UserGroups.Config do
  def user_groups do
    [
      %GroupSpec{
        name: "public",
        useage: [:write, :read_for_write, :read],
        access: %AlwaysAccessible{},
        graphs: [ %GraphSpec{
          graph: "http://mu.semte.ch/graphs/public",
          constraint: %ResourceConstraint{
            resource_types: [
            "http://data.vlaanderen.be/ns/generiek#GestructureerdeIdentificator",
            "http://www.w3.org/ns/locn#Address",
            "http://schema.org/ContactPoint",
            "http://data.vlaanderen.be/ns/mandaat#Mandaat",
            "http://www.w3.org/ns/org#Organization",
            "http://data.vlaanderen.be/ns/persoon#Person",
            "http://www.w3.org/ns/org#Site",
            "http://data.vlaanderen.be/ns/mandaat#Mandataris",
            "http://data.vlaanderen.be/ns/besluit#Bestuursorgaan",
            "http://data.vlaanderen.be/ns/mandaat#RechtstreekseVerkiezing"
            ]
          } } ] },
      %GraphCleanup{
        originating_graph: "http://mu.semte.ch/application",
        useage: [:read, :write],
        name: "clean"
      }
    ]
  end
end