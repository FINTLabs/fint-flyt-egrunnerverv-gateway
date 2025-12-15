apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: $NAMESPACE

resources:
  - ../../../base

labels:
  - pairs:
      app.kubernetes.io/instance: $APP_INSTANCE_LABEL
      fintlabs.no/org-id: $ORG_ID

patches:
  - patch: |-
      - op: replace
        path: "/spec/kafka/acls/0/topic"
        value: "$KAFKA_TOPIC"
      - op: replace
        path: "/spec/orgId"
        value: "$ORG_ID"
      - op: replace
        path: "/spec/ingress/basePath"
        value: "$INGRESS_BASE_PATH"
      - op: add
        path: "/spec/env/-"
        value:
          name: novari.kafka.topic.org-id
          value: "$NAMESPACE"$ENV_PATCHES$DISPATCH_PATCHES
    target:
      kind: Application
      name: fint-flyt-egrunnerverv-gateway
