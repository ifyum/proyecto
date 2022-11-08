proyecto ifyum
===

La base del proyecto se genero con **jhipster vx.x.x**. A partir del modelo `docs/modelo_er_v1.jh`. Se puede usar [https://start.jhipster.tech/jdl-studio/](https://start.jhipster.tech/jdl-studio/) para editar el modelo y regenerar el codigo base (aumentar version del modelo y guardar en misma carpeta).

**Como es una version antigua de jhipster se debe buscar la documentacion aqui: [https://www.jhipster.tech/documentation-archive/v4.14.5/creating-an-app/](https://www.jhipster.tech/documentation-archive/v4.14.5/creating-an-app/)**


## Desarrollo local

El proyecto se puede ejecutar con **Docker**, usando el siguiente comando. La configuracion esta en `docker/compose-dev.yml`.

```bash
docker-compose up -d -f --build compose-dev.yml
``` 

Esto compilara las imagenes en caso de ser necesario. Mas informacion en la [wiki](/wikis/desarrollo-local)

```bash
docker run -v ~/proyectos/ifyum/res-dis:/home/jhipster/app --name jhipster1 -p 8080:8080 -p 9000:9000 -p 3001:3001 -tid jhipster/jhipster:v4.14.5 tail -f /dev/null
```

Abrir dos consolas. En la primera:

```bash
docker exec -ti jhipster1 bash
./mvnw
```

En la segunda:

```bash
docker exec -ti jhipster1 bash
yarn install
yarn global add gulp-cli
gulp
```

## Testing

Este proyecto tiene por defecto configurado el testing framework. Por favor ejecutar los tests y mantenerlos, es decir actualizarlos cuando correspondan o crear nuevos si no existen.

```bash
docker exec -ti jhipster1 bash
./mvnw clean test
gulp test
```

### Templates para Issues y Merge Request

Se agregaron templates para la creacion de issues. Por favor deben usar estos templates para que todos tengamos claro los cambios que se publiquen en el repositorio.

