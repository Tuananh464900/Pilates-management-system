/**
 * Tiện ích chuẩn hóa và hiển thị tiếng Việt có dấu đầy đủ cho Frontend
 */
export const normalizeVietnameseText = (text: string | null | undefined): string => {
  if (!text) return '';
  const trimmed = text.trim();

  const mapping: Record<string, string> = {
    'Trang bi 8 may Reformer go soi cao cap.': 'Trang bị 8 máy Reformer gỗ sồi cao cấp.',
    'Trang bi tham tap lua va khung Tower.': 'Trang bị thảm tập lụa và khung Tower.',
    'Lop Reformer nang cao tang cuong co bung va can bang.': 'Lớp Reformer nâng cao tăng cường cơ bụng và cân bằng.',
    'Pilates tren tham tu nhien ket hop dung cu phuc hoi.': 'Pilates trên thảm tự nhiên kết hợp dụng cụ phục hồi.',
    'Bai tap chuyen sau voi khung thap Tower.': 'Bài tập chuyên sâu với khung tháp Tower.',
    'ha noi': 'Hà Nội',
    'Ha Noi': 'Hà Nội',
  };

  if (mapping[trimmed]) {
    return mapping[trimmed];
  }

  const replacements: [RegExp, string][] = [
    [/Trang bi 8 may Reformer go soi cao cap\.?/i, 'Trang bị 8 máy Reformer gỗ sồi cao cấp.'],
    [/Trang bi tham tap lua va khung Tower\.?/i, 'Trang bị thảm tập lụa và khung Tower.'],
    [/Lop Reformer nang cao tang cuong co bung va can bang\.?/i, 'Lớp Reformer nâng cao tăng cường cơ bụng và cân bằng.'],
    [/Pilates tren tham tu nhien ket hop dung cu phuc hoi\.?/i, 'Pilates trên thảm tự nhiên kết hợp dụng cụ phục hồi.'],
    [/Bai tap chuyen sau voi khung thap Tower\.?/i, 'Bài tập chuyên sâu với khung tháp Tower.'],
  ];

  for (const [pattern, replacement] of replacements) {
    if (pattern.test(trimmed)) {
      return replacement;
    }
  }

  return text;
};

