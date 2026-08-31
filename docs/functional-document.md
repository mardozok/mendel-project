# Documento funcional — Mendel Java Code Challenge

## 1. Objetivo

Implementar una API REST para almacenar y consultar transacciones en memoria.

## 2. Modelo funcional

Una transacción está compuesta por:

- `transaction_id`: identificador de la transacción.
- `amount`: monto de la transacción.
- `type`: tipo de transacción.
- `parent_id`: identificador opcional de la transacción padre.

Las relaciones padre-hijo permiten formar una estructura jerárquica.

En la implementación Java, `TransactionInputDTO` contiene el `id` de la transacción además de los campos recibidos en el body. El Controller toma el `transactionId` de la URL y construye el DTO completo antes de invocar al Service.

## 3. Alta de transacción

### Endpoint

`PUT /transactions/{transactionId}`

### Request

```json
{
  "amount": 5000,
  "type": "cars",
  "parentId": null
}
```

### Resultado esperado

```json
{
  "status": "ok"
}
```

El endpoint permite crear o reemplazar la transacción asociada al identificador.

## 4. Consulta por tipo

### Endpoint

`GET /transactions/types/{type}`

Devuelve una lista con los identificadores de todas las transacciones cuyo `type` coincide con el valor solicitado.

Si no existen coincidencias, la respuesta es una lista vacía.

## 5. Suma transitiva

### Endpoint

`GET /transactions/sum/{transactionId}`

Devuelve la suma del monto de la transacción indicada y de todos sus descendientes.

Ejemplo:

- Transacción 10: 5000
- Transacción 11: 10000, `parentId = 10`
- Transacción 12: 5000, `parentId = 11`

Resultado:

- `sum/10` = 20000
- `sum/11` = 15000

La consulta de 11 no incluye a su ancestro 10.

## 6. Validaciones

- `amount` es obligatorio.
- `amount` no puede ser negativo.
- `type` es obligatorio y no puede estar vacío.
- `parentId` es opcional.
- Si no existe la transacción consultada para calcular la suma, se devuelve HTTP 404.

## 7. Manejo de errores

Los errores del repository se encapsulan en excepciones propias y se devuelven como HTTP 500.

Las solicitudes inválidas se devuelven como HTTP 400.

## 8. Persistencia

El challenge no requiere SQL. Las transacciones se almacenan en memoria mediante `ConcurrentHashMap`.

## 9. Capas

### Controller

Expone los endpoints REST, valida el request y delega la ejecución al Service.

### Service

Contiene la lógica de negocio, el acceso al repository y el cálculo de la suma transitiva.

### Repository

Abstrae el acceso a las transacciones almacenadas en memoria.

## 10. Representación de datos

Los DTOs (`TransactionInputDTO`, `TransactionStatusDTO`, `TransactionSumDTO`) y el modelo `TransactionData` utilizan Java Records para reducir código ceremonial y mantenerlos inmutables.

## 11. Pruebas

Se incluyen:

- tests unitarios del Service;
- creación de transacción con y sin padre;
- reemplazo de una transacción existente;
- caso de tipo sin resultados;
- caso de transacción sin hijos;
- caso de suma transitiva;
- caso de no incluir ancestros;
- caso de transacción inexistente;
- errores del repository;
- validación de amount obligatorio/no negativo;
- validación de type obligatorio/no vacío;
- tests de integración de los endpoints.

## 12. Ejecución

La aplicación puede ejecutarse con Maven:

```bash
mvn clean test
mvn spring-boot:run
```

O mediante Docker:

```bash
docker compose up --build
```

La aplicación queda disponible en `http://localhost:8080`.
