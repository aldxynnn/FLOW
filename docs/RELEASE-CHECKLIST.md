# FLOW Release Checklist

## Build
- [ ] Backend tests pass
- [ ] Android debug/release build passes
- [ ] No secrets committed

## Authentication
- [ ] Login succeeds
- [ ] Invalid credentials rejected
- [ ] Expired/invalid JWT rejected
- [ ] Role-based endpoints return 403/401 correctly

## Driver workflow
- [ ] Assigned trips load
- [ ] Start trip updates state
- [ ] Foreground location service starts
- [ ] Location updates reach operations dashboard
- [ ] Complete trip updates state

## Offline-first
- [ ] START queues while offline
- [ ] COMPLETE queues while offline
- [ ] GPS samples buffer while offline
- [ ] WorkManager synchronizes after reconnect
- [ ] 4xx/5xx/401 behavior is handled without infinite retry

## Operations
- [ ] Trip creation validates driver and vehicle
- [ ] Dashboard metrics are correct
- [ ] WebSocket reconnect works

## Production
- [ ] `prod` profile uses external DB/JWT secrets
- [ ] Flyway migration applied
- [ ] Demo seeding disabled
- [ ] HTTPS configured
- [ ] WebSocket allowed origins restricted

## Portfolio
- [ ] README explains architecture and trade-offs
- [ ] Screenshots/demo flow captured
- [ ] CI status is green
- [ ] Release notes created
