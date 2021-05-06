export default [
  {
    match: {
      predicate: {
        type: 'uri',
        value: 'http://data.vlaanderen.be/ns/persoon#aanHef'
      }
    },
    callback: {
      method: 'POST',
      url: 'http://job-controller-service/delta'
    },
    options: {
      resourceFormat: 'v0.0.1',
      gracePeriod: 1000,
      ignoreFromSelf: true
    }
  }
];