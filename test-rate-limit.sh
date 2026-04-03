for i in {1..110}; do
  curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:8080/test-rate-limit?type=fixed_window"
done