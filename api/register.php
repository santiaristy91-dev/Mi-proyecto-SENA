<?php
/**
 * API ENDPOINT: Registro de Usuarios en MySQL
 * Conecta el formulario de la Landing Page de PowerGo directamente con la base de datos MySQL (powergo_db).
 */

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');
header('Content-Type: application/json; charset=UTF-8');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

$host = 'localhost';
$db   = 'powergo_db';
$user = 'root';
$pass = ''; // MySQL de Laragon
$charset = 'utf8mb4';

$dsn = "mysql:host=$host;dbname=$db;charset=$charset";
$options = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
    $pdo = new PDO($dsn, $user, $pass, $options);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Error de conexión a la base de datos: ' . $e->getMessage()]);
    exit;
}

// Leer datos (POST normal o JSON)
$input = json_decode(file_get_contents('php://input'), true);
$nombre   = $_POST['name']     ?? $input['name']     ?? '';
$email    = $_POST['email']    ?? $input['email']    ?? '';
$telefono = $_POST['phone']    ?? $input['phone']    ?? '';
$password = $_POST['password'] ?? $input['password'] ?? '';

if (empty(trim($nombre)) || empty(trim($email)) || empty(trim($password))) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Nombre, correo y contraseña son obligatorios.']);
    exit;
}

try {
    // 1. Verificar si el correo ya existe
    $stmtCheck = $pdo->prepare("SELECT id_usuario FROM usuarios WHERE email = ? LIMIT 1");
    $stmtCheck->execute([$email]);
    if ($stmtCheck->fetch()) {
        echo json_encode(['success' => false, 'message' => 'Este correo ya está registrado en PowerGo.']);
        exit;
    }

    // 2. Insertar en la base de datos MySQL
    $sql = "INSERT INTO usuarios (nombre_completo, email, telefono, password_hash, rol, saldo_cuenta, estado) 
            VALUES (?, ?, ?, ?, 'cliente', 25000.00, 'activo')";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$nombre, $email, $telefono, $password]);

    $nuevoId = $pdo->lastInsertId();

    echo json_encode([
        'success' => true,
        'message' => '¡Usuario registrado exitosamente en MySQL!',
        'id_usuario' => $nuevoId,
        'user' => [
            'id' => $nuevoId,
            'name' => $nombre,
            'email' => $email,
            'phone' => $telefono,
            'role' => 'cliente',
            'saldo' => 25000.00
        ]
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Error al guardar en MySQL: ' . $e->getMessage()]);
}
