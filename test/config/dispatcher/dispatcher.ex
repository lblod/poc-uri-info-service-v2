defmodule Dispatcher do
  use Matcher

  define_accept_types [
    json: ["application/json", "application/vnd.api+json"],
    html: ["text/html", "application/xhtml+html"],
    sparql: ["application/sparql-results+json"],
    any: ["*/*"]
  ]

  define_layers [:static, :sparql, :api_services, :frontend_fallback, :resources, :not_found]

  options "/*path", _ do
    conn
    |> Plug.Conn.put_resp_header("access-control-allow-headers", "content-type,accept")
    |> Plug.Conn.put_resp_header("access-control-allow-methods", "*")
    |> send_resp(200, "{ \"message\": \"ok\" }")
  end

  ###############
  # SPARQL
  ###############
  match "/sparql",
        %{
          layer: :sparql,
          accept: %{
            any: true
          }
        } do
    forward conn, [], "http://triplestore:8890/sparql"
  end

  ###############
  # Resource labels
  ###############
  match "/resource-labels/*path",
        %{
          layer: :api_services,
          accept: %{
            json: true
          }
        } do
    forward conn, path, "http://resource-labels/"
  end

  ###############
  # Uri Info V1
  ###############
  get "/uri-info/*path",
      %{
        layer: :api_services,
        accept: %{
          json: true
        }
      } do
    forward conn, path, "http://uri-info/"
  end

  ###############
  # API SERVICES
  ###############
  match "/model/*path",
        %{
          layer: :api_services,
          accept: %{
            json: true
          }
        } do
    forward conn, path, "http://uri-info-service-v2/model"
  end
  match "/meta/*path",
        %{
          layer: :api_services,
          accept: %{
            json: true
          }
        } do
    forward conn, path, "http://uri-info-service-v2/meta"
  end
  match "/page/*path",
        %{
          layer: :api_services,
          accept: %{
            json: true
          }
        } do
    forward conn, path, "http://uri-info-service-v2/page"
  end
  match "/update/*path",
        %{
          layer: :api_services,
          accept: %{
            json: true
          }
        } do
    forward conn, path, "http://uri-info-service-v2/update"
  end
  match "/list-options/*path",
        %{
          layer: :api_services,
          accept: %{
            json: true
          }
        } do
    forward conn, path, "http://uri-info-service-v2/list-options"
  end
  #################
  # NOT FOUND
  #################
  match "/*_path", %{layer: :not_found} do
    send_resp(conn, 404, "Route not found.  See config/dispatcher.ex")
  end

end