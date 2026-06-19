import { NextResponse } from "next/server";

type ErrorBody = {
  success: false;
  error: {
    code: string;
    message: string;
  };
};

export function successResponse<T>(data: T, status = 200) {
  return NextResponse.json({ success: true, data }, { status });
}

export function errorResponse(code: string, message: string, status = 400) {
  const body: ErrorBody = {
    success: false,
    error: { code, message }
  };

  return NextResponse.json(body, { status });
}
