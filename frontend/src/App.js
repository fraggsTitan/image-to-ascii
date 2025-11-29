import './App.css';
import "bootstrap/dist/css/bootstrap.min.css";
import {useState, useRef, useEffect, useMemo} from "react";

/* ---------------------- TITLE RIBBON ---------------------- */
function TitleRibbon() {
    return (
        <div
            className="w-100 py-3 text-center text-white"
            onClick={() => window.location.reload()}
            style={{
                background: "linear-gradient(135deg, #5b0aff 0%, #3a49f9 100%)",
                cursor: "pointer",
                fontSize: "1.9rem",
                fontWeight: "700",
                letterSpacing: "1px",
                userSelect: "none",
                boxShadow: "0 4px 18px rgba(0,0,0,0.4)"
            }}
        >
            Word Art Studio
        </div>
    );
}

/* ---------------------- CROP CANVAS COMPONENT ---------------------- */
function CropCanvas({ image, onCropChange }) {
    const canvasRef = useRef(null);
    const imgRef = useRef(null);

    const isDragging = useRef(false);
    const start = useRef({ x: 0, y: 0 });

    const draw = (cropBox = null) => {
        const canvas = canvasRef.current;
        const ctx = canvas.getContext("2d");

        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.drawImage(imgRef.current, 0, 0, canvas.width, canvas.height);

        if (cropBox) {
            ctx.strokeStyle = "red";
            ctx.lineWidth = 2;
            ctx.strokeRect(cropBox.x, cropBox.y, cropBox.w, cropBox.h);
        }
    };

    const handleMouseDown = (e) => {
        const rect = canvasRef.current.getBoundingClientRect();
        start.current = {
            x: e.clientX - rect.left,
            y: e.clientY - rect.top,
        };
        isDragging.current = true;
    };

    const handleMouseMove = (e) => {
        if (!isDragging.current) return;

        const rect = canvasRef.current.getBoundingClientRect();
        const mouseX = e.clientX - rect.left;
        const mouseY = e.clientY - rect.top;

        // Normalize rectangle so dragging in any direction works
        const x = Math.min(start.current.x, mouseX);
        const y = Math.min(start.current.y, mouseY);
        const w = Math.abs(mouseX - start.current.x);
        const h = Math.abs(mouseY - start.current.y);

        draw({ x, y, w, h });

        // SCALE to original image size
        const scaleX = imgRef.current.width / canvasRef.current.width;
        const scaleY = imgRef.current.height / canvasRef.current.height;

        onCropChange({
            startX: Math.round(x * scaleX),
            startY: Math.round(y * scaleY),
            width: Math.round(w * scaleX),
            height: Math.round(h * scaleY),
        });
    };

    const handleMouseUp = () => {
        isDragging.current = false;
    };

    useEffect(() => {
        const canvas = canvasRef.current;
        const ctx = canvas.getContext("2d");

        imgRef.current = new Image();
        imgRef.current.src = image;

        imgRef.current.onload = () => {
            canvas.width = 500;
            canvas.height =
                (imgRef.current.height / imgRef.current.width) * 500;

            draw();
        };
    }, [image]);

    return (
        <canvas
            ref={canvasRef}
            style={{
                border: "1px solid #333",
                borderRadius: "10px",
                cursor: "crosshair",
                maxWidth: "100%",
            }}
            onMouseDown={handleMouseDown}
            onMouseMove={handleMouseMove}
            onMouseUp={handleMouseUp}
        />
    );
}

/* ---------------------- MAIN APP ---------------------- */
export default function App() {

    const [mode, setMode] = useState("text");
    const [preview, setPreview] = useState(null);
    const [isGif, setIsGif] = useState(false);
    const [format, setFormat] = useState("");
    const [fileObj, setFileObj] = useState(null);
    /* CROP STATES */
    const [cropMode, setCropMode] = useState(false);
    const [crop, setCrop] = useState({
        startX: 0,
        startY: 0,
        width: 0,
        height: 0,
    });

    /* RESIZE STATES */
    const [finalWidth, setFinalWidth] = useState("");
    const [finalHeight, setFinalHeight] = useState("");
    const [scale, setScale] = useState(1);
    const[data,setData]=useState(null);//this stores the return text
    function handleFile(e) {
        if (e.target.files.length > 0) {
            const file = e.target.files[0];
            setFileObj(file);
            setData(null);
            // MIME type based detection
            const mime = file.type || "";
            let fmt = "";

            if (mime) {
                // mime looks like "image/png" or "image/jpeg"
                const parts = mime.split("/");
                if (parts.length === 2) {
                    fmt = parts[1].toLowerCase(); // "png", "jpeg", "gif", "webp"
                    if (fmt === "jpeg") fmt = "jpg"; // normalize jpeg -> jpg
                }
            }

            // fallback to file extension if MIME missing or unknown
            if (!fmt) {
                const name = file.name || "";
                const idx = name.lastIndexOf(".");
                if (idx !== -1) {
                    fmt = name.slice(idx + 1).toLowerCase();
                }
            }

            // final safety: if still empty, default to png
            if (!fmt) fmt = "png";

            setFormat(fmt);                  // store the format for backend
            setIsGif(fmt === "gif");         // detect GIFs
            setPreview(URL.createObjectURL(file));
            setCropMode(false);  // reset crop mode when new image is uploaded
        }
    }
    async function submitToBackend() {

        // FIX: only block when cropMode is ON
        if (cropMode && (crop.width === 0 || crop.height === 0)) return;

        const formdata = new FormData();
        formdata.append("file", fileObj);
        formdata.append("mapping", mode);

        if (finalWidth !== "") formdata.append("width", finalWidth);
        if (finalHeight !== "") formdata.append("height", finalHeight);

        formdata.append("scale", scale);
        formdata.append("format", format);

        if (cropMode) {
            formdata.append("cropWidth", crop.width);
            formdata.append("cropHeight", crop.height);
            formdata.append("x", crop.startX);
            formdata.append("y", crop.startY);
        }

        try {
            let endpoint = "";

            if (isGif) {
                endpoint = "http://localhost:8080/ascii/gif";
            } else if (cropMode) {
                endpoint = "http://localhost:8080/ascii/crop_and_build";
            } else {
                endpoint = "http://localhost:8080/ascii";
            }

            const res = await fetch(endpoint, {
                method: "POST",
                body: formdata
            });

            // FIX: Show backend errors in RenderBox
            if (!res.ok) {
                const errorText = await res.text();
                setData({ error: errorText });
                return;
            }

            const json = await res.json();
            setData(json);

        } catch (e) {
            setData({ error: e.message });
            console.error(e);
        }
    }
    return (
        <div
            className="App text-light min-vh-100 d-flex flex-column align-items-center"
            style={{ background: "#0d0f15" }}
        >
            <TitleRibbon />

            <div
                className="mt-5 d-flex flex-column align-items-center"
                style={{ width: "100%", maxWidth: "650px" }}
            >

                {/* ---------------------- UPLOAD CARD ---------------------- */}
                <div
                    className="p-4 rounded-4 shadow w-100"
                    style={{
                        background: "#171b22",
                        border: "1px solid #2b3040"
                    }}
                >
                    <h4 className="mb-3 fw-semibold text-info text-center">
                        📤 Upload Image
                    </h4>

                    <div
                        className="upload-dropzone"
                        onClick={() => document.getElementById("upload").click()}
                        onDragOver={(e) => {
                            e.preventDefault();
                            e.currentTarget.classList.add("drag-active");
                        }}
                        onDragLeave={(e) => {
                            e.currentTarget.classList.remove("drag-active");
                        }}
                        onDrop={(e) => {
                            e.preventDefault();
                            e.currentTarget.classList.remove("drag-active");

                            if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
                                const fakeEvent = { target: { files: e.dataTransfer.files } };
                                handleFile(fakeEvent);
                            }
                        }}
                        style={{
                            width: "100%",
                            padding: "50px 40px",
                            textAlign: "center",
                        }}
                    >
                        <div>
                            <h5 className="mb-2" style={{ color: "#c9d1d9", fontWeight: "600" }}>
                                Drag & Drop Image Here
                            </h5>

                            <span style={{ color: "#9ba3af", fontSize: "0.9rem" }}>
                or <strong style={{ color: "#58a6ff" }}>click</strong> to choose from computer
            </span>
                        </div>
                    </div>

                    {/* Hidden input */}
                    <input
                        id="upload"
                        type="file"
                        accept="image/*"
                        style={{ display: "none" }}
                        onChange={handleFile}
                    />

                    {/* Preview */}
                    {preview && (
                        <div className="mt-4 d-flex justify-content-center">
                            <img
                                src={preview}
                                alt="preview"
                                style={{
                                    maxWidth: "100%",
                                    maxHeight: "260px",
                                    borderRadius: "12px",
                                    border: "1px solid #333"
                                }}
                            />
                        </div>
                    )}
                </div>

                {/* ---------------------- MODE SLIDER ---------------------- */}
                <div
                    className="p-4 mt-4 rounded-4 shadow w-100"
                    style={{
                        background: "#171b22",
                        border: "1px solid #2b3040"
                    }}
                >
                    <h4 className="fw-semibold mb-3 text-warning text-center">🎛 Mode</h4>

                    <div className="d-flex align-items-center justify-content-center">
                        <span className="me-3">Text</span>

                        <label className="switch">
                            <input
                                type="checkbox"
                                checked={mode === "blocks"}
                                onChange={() =>
                                    setMode(mode === "text" ? "blocks" : "text")
                                }
                            />
                            <span className="slider round"></span>
                        </label>

                        <span className="ms-3">Blocks</span>
                    </div>
                </div>

                {/* ---------------------- CROP SLIDER ---------------------- */}
                {preview && !isGif && (
                    <div
                        className="p-4 mt-4 rounded-4 shadow w-100"
                        style={{
                            background: "#171b22",
                            border: "1px solid #2b3040"
                        }}
                    >
                        <h4 className="fw-semibold text-success text-center mb-3">
                            ✂️ Crop Image
                        </h4>

                        <div className="d-flex justify-content-center align-items-center mb-3">
                            <span className="me-3">Off</span>

                            <label className="switch">
                                <input
                                    type="checkbox"
                                    checked={cropMode}
                                    onChange={() => setCropMode(!cropMode)}
                                />
                                <span className="slider round"></span>
                            </label>

                            <span className="ms-3">On</span>
                        </div>

                        {cropMode && (
                            <div className="d-flex justify-content-center">
                                <CropCanvas
                                    image={preview}
                                    onCropChange={setCrop}
                                />
                            </div>
                        )}
                    </div>
                )}

                {/* ---------------------- RESIZE BOX ---------------------- */}
                <div
                    className="p-4 mt-4 rounded-4 shadow w-100"
                    style={{
                        background: "#171b22",
                        border: "1px solid #2b3040"
                    }}
                >
                    <h4 className="fw-semibold text-center text-info mb-3">
                        📐 Resize Output
                    </h4>

                    {/* Width & Height Inputs */}
                    <div className="d-flex justify-content-center gap-3 mb-3">
                        <input
                            type="number"
                            placeholder="Width"
                            value={finalWidth}
                            onChange={(e) => setFinalWidth(e.target.value)}
                            className="form-control text-center"
                            style={{ maxWidth: "120px", background: "#111", color: "white" }}
                        />

                        <input
                            type="number"
                            placeholder="Height"
                            value={finalHeight}
                            onChange={(e) => setFinalHeight(e.target.value)}
                            className="form-control text-center"
                            style={{ maxWidth: "120px", background: "#111", color: "white" }}
                        />
                    </div>

                    {/* Scale Dropdown */}
                    <div className="d-flex justify-content-center">
                        <select
                            value={scale}
                            onChange={(e) => setScale(Number(e.target.value))}
                            className="form-select text-center"
                            style={{
                                maxWidth: "140px",
                                background: "#111",
                                color: "white",
                                border: "1px solid #333",
                                borderRadius: "10px"
                            }}
                        >
                            <option value={1}>Scale ×1</option>
                            <option value={2}>Scale ×2</option>
                            <option value={4}>Scale ×4</option>
                            <option value={8}>Scale ×8</option>
                        </select>
                    </div>
                </div>

                {/* ---------------------- RENDER CODE BOX ---------------------- */}
                <div
                    className="p-4 mt-4 rounded-4 shadow w-100"
                    style={{
                        background: "#171b22",
                        border: "1px solid #2b3040"
                    }}
                >
                    <h4 className="fw-semibold mb-3 text-primary text-center">🧪 Render Code</h4>
                    <RenderBox data={data} isGif={isGif}/>
                </div>

                {/* ---------------------- SUBMIT BUTTON ---------------------- */}
                <div className="text-center mt-4 mb-5">
                    <button
                        className="btn btn-lg px-5 text-white"
                        style={{
                            background: "linear-gradient(135deg,#6a11cb,#2575fc)",
                            border: "none",
                            borderRadius: "12px",
                            padding: "12px 35px",
                            boxShadow: "0 4px 12px rgba(0,0,0,0.5)"
                        }}
                        onClick={()=>submitToBackend()}
                    >
                        Render
                    </button>
                </div>

            </div>
        </div>
    );
}
function RenderBox({ data, isGif }) {
    if (!data) return null;

    // 🔥 Handle errors
    if (data.error) {
        return (
            <pre style={{ color: "red", whiteSpace: "pre-wrap" }}>
                {data.error}
            </pre>
        );
    }
    console.log(data);
    // 🔥 Detect GIF data structure
    if (isGif) {
        if (Array.isArray(data)) {
            return <AsciiGifPlayer frames={data} />;
        }

        // backend returned { frames: [...] }
        if (Array.isArray(data.frames)) {
            return <AsciiGifPlayer frames={data.frames} />;
        }

        return <pre>No GIF data returned.</pre>;
    }

    // 🔥 Detect normal image format
    if (data) {
        return <ASCIIImageDisplay image={data} />;
    }

    return <pre>Invalid response format.</pre>;
}
function AsciiGifPlayer({ frames }) {
    const [index, setIndex] = useState(0);
    const [textSize, setTextSize] = useState(1.3);
    const [brightness, setBrightness] = useState(255); // NEW

    // Precompute decoded frames
    const { decodedFrames, delays, inverted } = useMemo(() => {
        if (!frames || frames.length === 0)
            return { decodedFrames: [], delays: [], inverted: [] };

        const decoded = [];
        const ds = [];
        const inv = [];

        for (const frame of frames) {
            let text = "";
            for (const { ch, freq } of frame.charMaps) text += ch.repeat(freq);

            decoded.push(text);
            ds.push(frame.delay || 100);

            // Detect full-block ASCII shading frames
            const isInverted = frame.charMaps.some(c =>
                ["█", "▓", "▒", "░"].includes(c.ch)
            );
            inv.push(isInverted);
        }

        return { decodedFrames: decoded, delays: ds, inverted: inv };
    }, [frames]);

    // Animation controller
    useEffect(() => {
        if (decodedFrames.length === 0) return;
        const timer = setTimeout(() => {
            setIndex(prev => (prev + 1) % decodedFrames.length);
        }, delays[index]);
        return () => clearTimeout(timer);
    }, [index, decodedFrames, delays]);

    if (decodedFrames.length === 0) return null;

    return (
        <div style={{ color: "white", textAlign: "center" }}>

            {/* Text Size Slider */}
            <div style={{ marginBottom: "10px" }}>
                <input
                    type="range"
                    min="0.5"
                    max="6"
                    step="0.1"
                    value={textSize}
                    onChange={(e) => setTextSize(parseFloat(e.target.value))}
                    style={{ width: "200px" }}
                />
                <div style={{ fontSize: "12px" }}>
                    Text Size: {textSize.toFixed(1)}px
                </div>
            </div>

            {/* Brightness Slider — NEW */}
            <div style={{ marginBottom: "10px" }}>
                <input
                    type="range"
                    min="50"
                    max="255"
                    step="5"
                    value={brightness}
                    onChange={(e) => setBrightness(parseInt(e.target.value))}
                    style={{ width: "200px" }}
                />
                <div style={{ fontSize: "12px" }}>
                    Brightness: {brightness}
                </div>
            </div>

            <pre
                style={{
                    fontFamily: "monospace",
                    whiteSpace: "pre",
                    fontSize: `${textSize}px`,
                    lineHeight: `${textSize}px`,
                    display: "inline-block",

                    color: inverted[index]
                        ? `rgb(${255 - brightness}, ${255 - brightness}, ${255 - brightness})`
                        : `rgb(${brightness}, ${brightness}, ${brightness})`,

                    backgroundColor: inverted[index] ? "white" : "black",
                }}
            >
                {decodedFrames[index]}
            </pre>
        </div>
    );
}

function ASCIIImageDisplay({ image }) {
    const [textSize, setTextSize] = useState(1.3);
    const [brightness, setBrightness] = useState(255);
    const [color, setColor] = useState("#ffffff");

    const { asciiText, inverted } = useMemo(() => {
        if (!image) return { asciiText: "", inverted: false };

        let text = "";
        for (const { ch, freq } of image) text += ch.repeat(freq);

        const isInverted = image.some(c =>
            ["█", "▓", "▒", "░"].includes(c.ch)
        );

        return { asciiText: text, inverted: isInverted };
    }, [image]);

    if (!asciiText) return null;

    // ⭐ Download handler
    const handleDownload = () => {
        const blob = new Blob([asciiText], { type: "text/plain" });
        const url = URL.createObjectURL(blob);

        const a = document.createElement("a");
        a.href = url;
        a.download = "ascii-art.txt";
        a.click();

        URL.revokeObjectURL(url);
    };

    return (
        <div style={{ color: "white", textAlign: "center" }}>

            {/* Text Size */}
            <div style={{ marginBottom: "10px" }}>
                <input
                    type="range"
                    min="0.5"
                    max="6"
                    step="0.1"
                    value={textSize}
                    onChange={(e) => setTextSize(parseFloat(e.target.value))}
                    style={{ width: "200px" }}
                />
                <div style={{ fontSize: "12px" }}>
                    Text Size: {textSize.toFixed(1)}px
                </div>
            </div>

            {/* Brightness */}
            <div style={{ marginBottom: "10px" }}>
                <input
                    type="range"
                    min="50"
                    max="255"
                    step="5"
                    value={brightness}
                    onChange={(e) => setBrightness(parseInt(e.target.value))}
                    style={{ width: "200px" }}
                />
                <div style={{ fontSize: "12px" }}>Brightness: {brightness}</div>
            </div>

            {/* Color Picker */}
            <div style={{ marginBottom: "10px" }}>
                <input
                    type="color"
                    value={color}
                    disabled={inverted}
                    onChange={(e) => setColor(e.target.value)}
                />
                <div style={{ fontSize: "12px" }}>
                    Text Color: {color} {inverted ? "(auto-inverted)" : ""}
                </div>
            </div>

            {/* ⭐ Download TXT Button */}
            <div style={{ marginBottom: "15px" }}>
                <button
                    onClick={handleDownload}
                    style={{
                        padding: "8px 16px",
                        fontSize: "14px",
                        cursor: "pointer",
                        borderRadius: "6px",
                    }}
                >
                    Download ASCII (TXT)
                </button>
            </div>

            <pre
                style={{
                    fontFamily: "monospace",
                    whiteSpace: "pre",
                    fontSize: `${textSize}px`,
                    lineHeight: `${textSize}px`,
                    display: "inline-block",

                    color: inverted
                        ? `rgb(${255 - brightness}, ${255 - brightness}, ${255 - brightness})`
                        : color,

                    backgroundColor: inverted ? "white" : "black"
                }}
            >
                {asciiText}
            </pre>
        </div>
    );
}

