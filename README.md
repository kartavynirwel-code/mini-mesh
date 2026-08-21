# Mini-Mesh

A hands-on Kubernetes + Istio service mesh learning project.

```
Frontend (React)
   -> greeting-service (Spring Boot, Java)   GET /hello/{name}
       -> user-service (Spring Boot, Java)   GET /user/{name}
           -> notification-service (FastAPI) POST /notify   (fire-and-forget)
```

`greeting-service` blocks on its call to `user-service` because it needs
that data to answer the frontend. `user-service` does **not** block on its
call to `notification-service` — that's a side effect, not something the
caller asked for, so it's fired asynchronously and logged either way.

## Repo layout

```
mini-mesh/
├── frontend/                    React + Vite app                 (app code: done)
├── services/
│   ├── notification-service/    FastAPI                          (app code: done)
│   ├── user-service/            Spring Boot                      (app code: done)
│   └── greeting-service/        Spring Boot                      (app code: done)
├── k8s/                         Deployments, Services, ConfigMaps, Ingress  (you write these)
└── istio/                       PeerAuthentication, VirtualService, DestinationRule (you write these)
```

Each `services/*` and `frontend/` folder has a `.gitkeep-note.txt` marking
where your Dockerfile and `.dockerignore` go — delete those notes once you've
added the real files.

## Service contract reference

| Service | Port | Health endpoint | Env vars it reads | Points at |
|---|---|---|---|---|
| notification-service | 8000 | `GET /health` | *(none)* | — |
| user-service | 8080 | `GET /actuator/health` | `NOTIFICATION_SERVICE_URL` | `http://notification-service.mini-mesh.svc.cluster.local:8000` |
| greeting-service | 8080 | `GET /actuator/health` | `USER_SERVICE_URL` | `http://user-service.mini-mesh.svc.cluster.local:8080` |
| frontend | 80 (nginx) | — | `GREETING_SERVICE_URL` (consumed by your entrypoint script, see `frontend/public/config.template.js`) | the externally-reachable URL/path for greeting-service |

Keep your ConfigMap keys named exactly like the env var column above — the
application code reads those exact names via Spring's `${VAR:default}`
placeholder syntax (Java) and doesn't read anything for
notification-service since it has no downstream dependency.

## Build (once your Dockerfiles exist)

```bash
# from the mini-mesh/ root
docker build -t notification-service:1.0.0 services/notification-service
docker build -t user-service:1.0.0 services/user-service
docker build -t greeting-service:1.0.0 services/greeting-service
docker build -t frontend:1.0.0 frontend

# if using Minikube, load images into its Docker daemon rather than pushing
# to a registry:
minikube image load notification-service:1.0.0
minikube image load user-service:1.0.0
minikube image load greeting-service:1.0.0
minikube image load frontend:1.0.0
```

## Deploy (once your YAMLs exist)

Order matters here — labeling the namespace for Istio sidecar injection
has to happen *before* pods are created, since injection is a mutating
admission webhook that only fires at pod-creation time:

```bash
kubectl apply -f k8s/namespace/            # creates mini-mesh, labels istio-injection=enabled
kubectl apply -f k8s/configmaps/
kubectl apply -f k8s/notification-service/
kubectl apply -f k8s/user-service/
kubectl apply -f k8s/greeting-service/
kubectl apply -f k8s/frontend/
kubectl apply -f istio/
```

If you applied Deployments before labeling the namespace, force pods
through the webhook after the fact:

```bash
kubectl rollout restart deployment -n mini-mesh --all
```

## Verification checklist

1. **Pods are healthy and meshed**
   ```bash
   kubectl get pods -n mini-mesh
   ```
   Every pod should show `2/2` under READY (your app container + the
   `istio-proxy` sidecar). `1/1` means injection didn't happen — check
   the namespace label and pod creation order above.

2. **Direct health checks** (port-forward each Service):
   ```bash
   kubectl port-forward -n mini-mesh svc/notification-service 8000:8000
   curl http://localhost:8000/health

   kubectl port-forward -n mini-mesh svc/user-service 8080:8080
   curl http://localhost:8080/actuator/health

   kubectl port-forward -n mini-mesh svc/greeting-service 8081:8080
   curl http://localhost:8081/actuator/health
   ```

3. **End-to-end call through the chain**:
   ```bash
   curl http://localhost:8081/hello/Alice
   ```
   Expect: `{"greeting":"Hello, Alice!","userDetails":{"userId":"...","name":"Alice","joinedDate":"..."}}`

4. **Notification actually fired** — check the logs of both services
   that participate in that side effect:
   ```bash
   kubectl logs -n mini-mesh deployment/user-service -c user-service | grep -i notification
   kubectl logs -n mini-mesh deployment/notification-service -c notification-service | grep -i "Notification sent"
   ```

5. **Frontend works end-to-end** — hit its Ingress/NodePort URL in a
   browser, type a name, click Greet, and confirm the combined response
   renders (greeting + userId + joinedDate).

6. **mTLS is actually STRICT**, not just declared:
   ```bash
   kubectl exec -n mini-mesh deploy/greeting-service -c istio-proxy -- \
     openssl s_client -connect user-service:8080 2>&1 | grep -i "verify"
   ```
   A plaintext `curl` from a pod *without* a sidecar to any meshed
   Service should fail once `PeerAuthentication` is STRICT — that's a
   good negative test to try (e.g. `kubectl run test --image=curlimages/curl -n mini-mesh -it --rm -- curl http://user-service:8080/user/test`,
   run *before* your PeerAuthentication is applied vs. after).

## Kiali dashboard

```bash
istioctl dashboard kiali
```

Things to check once traffic has flowed through the system (hit the
frontend a few times first — Kiali graphs recent traffic, not history):

- **Service graph**: you should see four nodes — frontend, greeting-service,
  user-service, notification-service — with directional edges matching the
  call chain above. If a node is missing an edge, that call either hasn't
  happened yet or is failing silently — check the source pod's `istio-proxy`
  logs.
- **Padlock icons on edges**: a closed padlock on an edge means mTLS is
  active for that connection. With `PeerAuthentication` set to STRICT
  across the namespace, every edge inside the mesh should show one. No
  padlock (or a broken one) means something's bypassing the sidecar or
  your PeerAuthentication scope doesn't cover that workload.
- **Edge colors**: green means healthy traffic; red/yellow indicates
  error rates or high latency on that hop — a good early signal if your
  VirtualService or DestinationRule for greeting-service is misconfigured.
