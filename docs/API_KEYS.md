# 🔐 API 키 설정 가이드

> **중요**: 이 파일에는 실제 API 키가 포함되어 있습니다. 절대 Git에 커밋하지 마세요!

## 📝 설정 방법

1. `.env.template`을 복사하여 `.env` 파일 생성:
   ```bash
   cp .env.template .env
   ```

2. `.env` 파일을 열고 실제 API 키 입력:

### Cafe24 API 키
```bash
CAFE24_CLIENT_ID=qiLpnOXHBtIuke056I0FvD
CAFE24_CLIENT_SECRET=ifcidJ5MVa3TJ2lkzUxdNH
CAFE24_SERVICE_KEY=fS/FHhPbM2tO0sLuG98FiotvcsOalTc1Oa4UfQbeNEo=
CAFE24_MALL_ID=your_actual_mall_id  # 실제 쇼핑몰 ID로 변경
```

### KakaoPay API 키
```bash
KAKAOPAY_CLIENT_ID=119A171B128E6FB4C534
KAKAOPAY_CLIENT_SECRET=EE60EE240E69BA4C928C
KAKAOPAY_SECRET_KEY_DEV=DEVE05C43E8449B99D99D9CE8194554F2F7FBA0F
KAKAOPAY_SECRET_KEY_PROD=PRD4FC8A5EDFC7F859599B527D391649086EB0E2
```

## ⚠️ 보안 주의사항

- `.env` 파일은 `.gitignore`에 포함되어 있어 Git에 커밋되지 않습니다
- 절대 `.env` 파일을 공개 저장소에 업로드하지 마세요
- 팀원과 공유할 때는 안전한 채널(1Password, Vault 등)을 사용하세요
- 프로덕션 환경에서는 환경 변수 또는 시크릿 관리 서비스를 사용하세요

## 🔍 확인

설정이 올바르게 되었는지 확인:
```bash
# .env 파일이 Git에 추가되지 않았는지 확인
git status

# .env 파일이 목록에 나타나면 안 됩니다!
```
