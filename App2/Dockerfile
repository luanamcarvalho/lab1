FROM python:3.12-slim
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser
WORKDIR /app
COPY requirements.txt /app
RUN pip install --no-cache-dir -r requirements.txt
COPY --chown=appuser:appgroup app.py /app
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
CMD ["python", "app.py"]
