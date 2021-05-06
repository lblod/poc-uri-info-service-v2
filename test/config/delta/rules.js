export default [
  {
    match: {
    },
    callback: {
      method: 'POST',
      url: 'http://kalliope/delta'
    },
    options: {
      resourceFormat: 'v0.0.1',
      gracePeriod: 1000,
      ignoreFromSelf: true
    }
  }
];