# Kubernetes Deployment for ProcessMonster

This directory contains Kubernetes manifests for deploying ProcessMonster Banking BPM to a Kubernetes cluster.

## Prerequisites

- Kubernetes cluster (v1.24+)
- kubectl configured
- cert-manager (for TLS certificates)
- nginx-ingress-controller

## Quick Start

### 1. Create Namespace

```bash
kubectl create namespace processmonster
```

### 2. Update Secrets

**IMPORTANT**: Update the secrets in `deployment.yaml` before deploying:

```bash
# Generate base64 encoded secrets
echo -n "your-strong-password" | base64
echo -n "your-jwt-secret-key" | base64
```

Update the `postgres-secret` and `app-secret` in `deployment.yaml`.

### 3. Deploy

```bash
kubectl apply -f deployment.yaml
```

### 4. Verify Deployment

```bash
# Check pods
kubectl get pods -n processmonster

# Check services
kubectl get svc -n processmonster

# Check ingress
kubectl get ingress -n processmonster
```

### 5. Access Application

```bash
# Get ingress IP
kubectl get ingress processmonster-ingress -n processmonster

# Or use port-forward for testing
kubectl port-forward -n processmonster svc/frontend 8080:80
```

## Scaling

### Manual Scaling

```bash
# Scale backend
kubectl scale deployment backend --replicas=5 -n processmonster

# Scale frontend
kubectl scale deployment frontend --replicas=3 -n processmonster
```

### Auto-scaling

HorizontalPodAutoscaler is configured for the backend:
- Min replicas: 2
- Max replicas: 10
- Target CPU: 70%
- Target Memory: 80%

## Monitoring

```bash
# View logs
kubectl logs -f -l app=backend -n processmonster
kubectl logs -f -l app=frontend -n processmonster

# View events
kubectl get events -n processmonster

# Describe resources
kubectl describe pod <pod-name> -n processmonster
```

## Database Backup

```bash
# Backup database
kubectl exec -n processmonster postgres-0 -- pg_dump -U processmonster_user processmonster > backup.sql

# Restore database
kubectl exec -i -n processmonster postgres-0 -- psql -U processmonster_user processmonster < backup.sql
```

## Update Deployment

```bash
# Update image
kubectl set image deployment/backend backend=your-registry/processmonster-backend:v2.0.0 -n processmonster

# Rollout status
kubectl rollout status deployment/backend -n processmonster

# Rollback
kubectl rollout undo deployment/backend -n processmonster
```

## Clean Up

```bash
kubectl delete namespace processmonster
```

## Production Recommendations

1. **Use External Secrets Manager**: Replace hard-coded secrets with AWS Secrets Manager, HashiCorp Vault, or Google Secret Manager
2. **Setup Monitoring**: Install Prometheus & Grafana for metrics
3. **Setup Logging**: Configure ELK stack or Loki for centralized logging
4. **Configure Backups**: Use Velero for cluster backups
5. **Setup CI/CD**: Integrate with ArgoCD or Flux for GitOps
6. **Enable Network Policies**: Restrict traffic between pods
7. **Configure Resource Limits**: Set appropriate requests and limits
8. **Use Pod Disruption Budgets**: Ensure high availability
