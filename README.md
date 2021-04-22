# [URI-INFO-SERVICE-V2]

This service generates the structure of an UI based on a subject uri.

- Fetch the meta model of the UI (based on the type of the subject)
- Fetch the full model based on the subject uri
- Update properties of the model
- Generate the structure (JSON) of the UI based on a subject uri

## Development

In case you want to test a change, but don't have java/maven installed on your machine, you can use the dummy
docker-compose stack provided for this purpose.

- `cd ./test`
- `docker-compose build`
- `docker-compose up`
- Wait that the migration service has finished
- Check the page generated for person in your
  browser: `http://localhost/page?uri=http://data.lblod.info/id/persoon/5a616d80d0c2908573bd27bdb5aefee7`
- Check the meta model for person in your
  browser: `http://localhost/meta?uri=http://data.lblod.info/id/persoon/5a616d80d0c2908573bd27bdb5aefee7`
- Check the model for person in your
  browser: `http://localhost/model?uri=http://data.lblod.info/id/persoon/5a616d80d0c2908573bd27bdb5aefee7`
- Open postman
- Run the request below

`POST localhost/update`

```
    {
        "subject": "http://data.lblod.info/id/contactpunt/fd195a19d605c4d0b299a27881fb3fb9",
        "predicate": "http://xmlns.com/foaf/0.1/page",
        "object": "http://www.yaya.be",
        "datatype": "http://www.w3.org/2001/XMLSchema#anyURI",
        "language": ""
    }

```

- check that the value has been
  updated : `http://localhost/page?uri=http://data.lblod.info/id/persoon/5a616d80d0c2908573bd27bdb5aefee7`
