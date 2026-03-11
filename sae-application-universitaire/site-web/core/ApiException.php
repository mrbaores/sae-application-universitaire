<?php
final class ApiException extends Exception {
    public int $status;
    public array $payload;

    public function __construct(string $message, int $status = 0, array $payload = []) {
        parent::__construct($message);
        $this->status = $status;
        $this->payload = $payload;
    }
}
