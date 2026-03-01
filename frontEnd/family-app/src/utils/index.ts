export const formatDate = (date: Date | number | string, format = 'YYYY-MM-DD HH:mm') => {
  const target = typeof date === 'string' ? new Date(date) : new Date(date);
  const pad = (n: number) => n.toString().padStart(2, '0');
  const year = target.getFullYear();
  const month = pad(target.getMonth() + 1);
  const day = pad(target.getDate());
  const hour = pad(target.getHours());
  const minute = pad(target.getMinutes());

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute);
};

export const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));
