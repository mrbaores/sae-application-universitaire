<?php
return [
    'db' => [
        'host' => 'localhost',
        'name' => 'nom_de_la_base',
        'user' => 'utilisateur_mysql',
        'pass' => 'mot_de_passe_mysql',
        'charset' => 'utf8mb4',
    ],
    'jwt_secret' => 'dev-secret-change-me',
    'cors' => [
        'allow_origin' => '*',
        'allow_headers' => 'Content-Type, Authorization',
        'allow_methods' => 'GET, POST, PUT, PATCH, DELETE, OPTIONS',
    ],
];
