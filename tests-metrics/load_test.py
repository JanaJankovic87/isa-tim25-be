"""
Load Test Script za JutJubic aplikaciju
Generiše ~200-250 req/s za testiranje monitoring sistema
"""

import requests
import threading
import time
from collections import defaultdict

# Konfiguracija
URL = "http://localhost:8082/api/videos/"
THREADS = 50
TEST_DURATION = 60  # sekundi

# Globalni brojači
stats = {
    'total': 0,
    'success': 0,
    'errors': 0,
    'response_times': []
}
lock = threading.Lock()

def make_requests():
    """Worker thread koji šalje HTTP zahteve"""
    start_time = time.time()

    while time.time() - start_time < TEST_DURATION:
        try:
            req_start = time.time()
            response = requests.get(URL, timeout=5)
            req_duration = time.time() - req_start

            with lock:
                stats['total'] += 1
                if response.status_code == 200:
                    stats['success'] += 1
                else:
                    stats['errors'] += 1
                stats['response_times'].append(req_duration)

        except Exception as e:
            with lock:
                stats['total'] += 1
                stats['errors'] += 1

def print_header():
    """Ispis zaglavlja"""
    print("\n" + "=" * 70)
    print("JUTJUBIC LOAD TEST")
    print("=" * 70)
    print(f" Target URL:     {URL}")
    print(f"Threads:        {THREADS}")
    print(f" Duration:       {TEST_DURATION} seconds")
    print(f" Expected Rate:  ~200-250 req/s")
    print("=" * 70)
    print("\n Open Grafana NOW: http://localhost:3000")
    print("  Press Ctrl+C to stop early\n")
    print("-" * 70)

def print_progress(elapsed):
    """Ispis progressa tokom testa"""
    with lock:
        rate = stats['total'] / elapsed if elapsed > 0 else 0
        success_rate = (stats['success'] / stats['total'] * 100) if stats['total'] > 0 else 0

        print(f" {int(elapsed):3d}s | "
              f"Requests: {stats['total']:5d} | "
              f"Rate: {rate:6.1f} req/s | "
              f"Success: {stats['success']:5d} ({success_rate:5.1f}%) | "
              f"Errors: {stats['errors']:4d}")

def print_results(total_time):
    """Ispis finalnih rezultata"""
    avg_response_time = sum(stats['response_times']) / len(stats['response_times']) if stats['response_times'] else 0
    success_rate = (stats['success'] / stats['total'] * 100) if stats['total'] > 0 else 0

    print("\n" + "=" * 70)
    print(" FINAL RESULTS")
    print("=" * 70)
    print(f"Total Requests:        {stats['total']:,}")
    print(f"Successful:            {stats['success']:,} ({success_rate:.1f}%)")
    print(f"Failed:                {stats['errors']:,} ({100-success_rate:.1f}%)")
    print(f"Total Duration:        {total_time:.1f} seconds")
    print(f"Average Rate:          {stats['total']/total_time:.1f} req/sec")
    print(f"Avg Response Time:     {avg_response_time*1000:.1f} ms")

    if stats['response_times']:
        sorted_times = sorted(stats['response_times'])
        p50 = sorted_times[len(sorted_times)//2] * 1000
        p95 = sorted_times[int(len(sorted_times)*0.95)] * 1000
        p99 = sorted_times[int(len(sorted_times)*0.99)] * 1000

        print(f"Response Time p50:     {p50:.1f} ms")
        print(f"Response Time p95:     {p95:.1f} ms")
        print(f"Response Time p99:     {p99:.1f} ms")

    print("=" * 70)
    print("\nTest complete! Check Grafana for visual metrics.")
    print(" Grafana: http://localhost:3000")
    print(" Prometheus: http://localhost:9090\n")

def main():
    """Glavna funkcija"""
    print_header()

    # Proveri da li server radi
    try:
        response = requests.get(URL, timeout=5)
        print(f" Server is reachable (Status: {response.status_code})\n")
    except Exception as e:
        print(f" ERROR: Cannot reach {URL}")
        print(f"   Make sure Spring Boot app is running on port 8082")
        print(f"   Error: {e}\n")
        return

    # Pokreni worker thread-ove
    threads = []
    test_start = time.time()

    for i in range(THREADS):
        t = threading.Thread(target=make_requests, daemon=True)
        t.start()
        threads.append(t)

    # Monitor progress
    try:
        last_print = 0
        while time.time() - test_start < TEST_DURATION:
            elapsed = time.time() - test_start

            # Print progress svaki 5 sekundi
            if elapsed - last_print >= 5:
                print_progress(elapsed)
                last_print = elapsed

            time.sleep(1)

    except KeyboardInterrupt:
        print("\n\n⚠️  Test interrupted by user!")

    # Sačekaj da thread-ovi završe
    print("\n⏳ Waiting for threads to finish...")
    for t in threads:
        t.join(timeout=5)

    # Finalni rezultati
    total_time = time.time() - test_start
    print_results(total_time)

if __name__ == "__main__":
    main()